package prog2.policia_frontend.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.PollEvent;
import com.vaadin.flow.shared.Registration;

import prog2.policia_frontend.clients.AutenticacionClient;
import prog2.policia_frontend.clients.EstadoBackendClient;
import prog2.policia_frontend.security.ResultadoAutenticacion;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.utils.NotificacionUtils;

@Route("login")
@PageTitle("Ingreso | Sistema Policial")
@StyleSheet("context://styles/login.css")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AutenticacionClient autenticacionClient;
    private final SesionUsuario sesionUsuario;
    private final EstadoBackendClient estadoBackendClient;
    private volatile boolean backendDisponible;
    private Thread hiloConexion;
    private UI uiConexion;
    private Registration registroConsulta;
    private int intervaloAnterior;

    private final LoginForm formularioLogin = new LoginForm();
    private final Span mensajeConexion = new Span("Conectando con el servidor...");

    public LoginView(AutenticacionClient autenticacionClient, SesionUsuario sesionUsuario,
            EstadoBackendClient estadoBackendClient) {
        this.autenticacionClient = autenticacionClient;
        this.sesionUsuario = sesionUsuario;
        this.estadoBackendClient = estadoBackendClient;

        configurarVista();
        configurarFormularioLogin();
    }

    private void configurarVista() {
        H1 titulo = new H1("Sistema de Gestión Policial");
        titulo.addClassName("login-titulo");

        Span subtitulo = new Span("Acceso seguro al sistema");
        subtitulo.addClassName("login-subtitulo");

        Image imagenAsisa = new Image("images/asisa-policia.png", "Asisa con chaleco policial");
        imagenAsisa.addClassNames("login-gato", "login-gato-asisa");

        Image imagenCopo = new Image("images/copo-policia.png", "Copo con chaleco policial");
        imagenCopo.addClassNames("login-gato", "login-gato-copo");

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(false);
        setSpacing(false);
        addClassName("login-view");

        formularioLogin.addClassName("login-formulario");
        formularioLogin.setVisible(false);

        VerticalLayout tarjeta = new VerticalLayout(titulo, subtitulo, mensajeConexion, formularioLogin);
        tarjeta.setAlignItems(Alignment.CENTER);
        tarjeta.setPadding(true);
        tarjeta.setWidth("410px");
        tarjeta.setMaxWidth("88vw");
        tarjeta.addClassName("login-tarjeta");

        add(imagenAsisa, tarjeta, imagenCopo);
    }

    private void configurarFormularioLogin() {
        LoginI18n textos = new LoginI18n();

        LoginI18n.Form formulario = new LoginI18n.Form();
        formulario.setTitle("Ingresar");
        formulario.setUsername("Código de usuario");
        formulario.setPassword("Contraseña");
        formulario.setSubmit("Ingresar");
        formulario.setForgotPassword("Olvidé mi contraseña");
        textos.setForm(formulario);

        LoginI18n.ErrorMessage mensajeError = new LoginI18n.ErrorMessage();
        mensajeError.setTitle("No se pudo ingresar");
        mensajeError.setMessage("Código o contraseña incorrectos.");
        mensajeError.setUsername("El código es obligatorio.");
        mensajeError.setPassword("La contraseña es obligatoria.");
        textos.setErrorMessage(mensajeError);

        formularioLogin.setI18n(textos);
        formularioLogin.setForgotPasswordButtonVisible(false);
        formularioLogin.addLoginListener(this::intentarIngresar);
    }

    private void intentarIngresar(AbstractLogin.LoginEvent evento) {
        String codigoIngresado = evento.getUsername();
        String contrasenaIngresada = evento.getPassword();

        formularioLogin.setError(false);
        formularioLogin.setEnabled(false);

        try {
            ResultadoAutenticacion resultado = autenticacionClient.autenticar(codigoIngresado, contrasenaIngresada);
            sesionUsuario.iniciarSesion(resultado, contrasenaIngresada);
            UI.getCurrent().navigate(InicioView.class);

        } catch (RestClientResponseException excepcion) {
            limpiarContrasena();
            int estado = excepcion.getStatusCode().value();

            switch (estado) {
                case 401 ->
                    formularioLogin.setError(true);
                case 403 ->
                    NotificacionUtils.mostrarInformacion("El usuario no tiene permiso para ingresar.");
                case 429 ->
                    NotificacionUtils.mostrarInformacion("Demasiados intentos. Probá nuevamente en unos minutos.");
                case 500, 502, 503, 504 ->
                    NotificacionUtils.mostrarInformacion("El servidor tuvo un problema. Intentá nuevamente.");
                default -> {
                    java.util.logging.Logger.getLogger(LoginView.class.getName())
                            .log(java.util.logging.Level.WARNING, "No se pudo iniciar sesión. Estado HTTP: {0}", estado);
                    NotificacionUtils.mostrarInformacion("No se pudo iniciar sesión. Intentá nuevamente.");
                }
            }

        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el servidor.");

        } catch (IllegalArgumentException excepcion) {
            NotificacionUtils.mostrarInformacion(excepcion.getMessage());

        } finally {
            formularioLogin.setEnabled(true);
        }
    }

    // Borra la contraseña y conserva el código ingresado.
    private void limpiarContrasena() {
        formularioLogin.getElement().executeJs("""
            const campo = this.querySelector('vaadin-password-field');
            if (campo) {
                campo.value = '';
                campo.focus();
            }
        """);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        // Cierra la sesión anterior al volver al login.
        if (sesionUsuario.estaAutenticado()) {
            sesionUsuario.cerrarSesion();
        }
        formularioLogin.setError(false);
    }

    //consulta al backend en segundo plano y vuelve a intentar si todavía no responde
    private void iniciarComprobacion() {
        backendDisponible = false;

        //Cuando el backend responde OK, el hilo termina
        hiloConexion = Thread.ofVirtual().start(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    if (estadoBackendClient.estaDisponible()) {
                        backendDisponible = true;
                        return;
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException excepcion) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        });
    }

    //revisa cada segundo si el hilo recibió OK. Cuando recibe, muestra login y detiene la comprobación.
    @Override
    protected void onAttach(AttachEvent evento) {
        super.onAttach(evento);

        uiConexion = evento.getUI();
        intervaloAnterior = uiConexion.getPollInterval();

        mensajeConexion.setVisible(true);
        formularioLogin.setVisible(false);

        registroConsulta = uiConexion.addPollListener(new ComponentEventListener<PollEvent>() {
            @Override
            public void onComponentEvent(PollEvent consulta) {
                if (backendDisponible) {
                    detenerComprobacion();
                    mensajeConexion.setVisible(false);
                    formularioLogin.setVisible(true);
                }
            }
        });

        uiConexion.setPollInterval(1000);
        iniciarComprobacion();
    }

    @Override
    protected void onDetach(DetachEvent evento) {
        detenerComprobacion();
        super.onDetach(evento);
    }

    private void detenerComprobacion() {
        if (hiloConexion != null) {
            hiloConexion.interrupt();
            hiloConexion = null;
        }

        if (registroConsulta != null) {
            registroConsulta.remove();
            registroConsulta = null;
        }

        if (uiConexion != null) {
            uiConexion.setPollInterval(intervaloAnterior);
            uiConexion = null;
        }
    }
}
