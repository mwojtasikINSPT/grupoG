package prog2.policia_frontend.views;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import prog2.policia_frontend.clients.AutenticacionClient;
import prog2.policia_frontend.security.SesionUsuario;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.dtos.UsuarioDTO;

/*
 * Pantalla utilizada para iniciar sesión
 * en el sistema policial.
 */
@Route("login")
@PageTitle("Ingreso | Sistema Policial")
@StyleSheet("context://styles/login.css")
public class LoginView extends VerticalLayout implements BeforeEnterObserver{

    private final AutenticacionClient autenticacionClient;
    private final SesionUsuario sesionUsuario;

    /*
     * Componente oficial de Vaadin que contiene
     * el usuario, la contraseña y el botón de ingreso.
     */
    private final LoginForm formularioLogin
            = new LoginForm();

    /*
     * Spring entrega automáticamente el cliente
     * y la sesión que necesita esta vista.
     */
    public LoginView(
            AutenticacionClient autenticacionClient,
            SesionUsuario sesionUsuario) {

        this.autenticacionClient = autenticacionClient;
        this.sesionUsuario = sesionUsuario;

        configurarVista();
        configurarFormularioLogin();
    }

    /*
     * Organiza visualmente los componentes.
     */
    private void configurarVista() {

        H1 titulo = new H1(
                "Sistema de Gestión Policial"
        );

        titulo.addClassName("login-titulo");

        Span subtitulo = new Span(
                "Acceso seguro al sistema"
        );
        subtitulo.addClassName("login-subtitulo");

        /*
         * Los gatos son elementos decorativos. El texto alternativo
         * permite comprender la imagen si esta no puede mostrarse.
         */
        Image imagenAsisa = new Image(
                "images/asisa-policia.png",
                "Asisa con chaleco policial"
        );
        imagenAsisa.addClassNames(
                "login-gato",
                "login-gato-asisa"
        );

        Image imagenCopo = new Image(
                "images/copo-policia.png",
                "Copo con chaleco policial"
        );
        imagenCopo.addClassNames(
                "login-gato",
                "login-gato-copo"
        );

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(
                JustifyContentMode.CENTER
        );

        setPadding(false);
        setSpacing(false);
        addClassName("login-view");

        

        formularioLogin.addClassName("login-formulario");

        VerticalLayout tarjeta = new VerticalLayout(
                titulo,
                subtitulo,
                formularioLogin
        );

        tarjeta.setAlignItems(Alignment.CENTER);
        tarjeta.setPadding(true);
        tarjeta.setWidth("410px");
        tarjeta.setMaxWidth("88vw");
        tarjeta.addClassName("login-tarjeta");
        
        add(
                imagenAsisa,
                tarjeta,
                imagenCopo
        );
    }
    /*
 * Configura los textos en español y conecta el formulario
 * oficial de Vaadin con la autenticación existente.
 */
private void configurarFormularioLogin() {

    LoginI18n textos = new LoginI18n();

    LoginI18n.Form formulario = new LoginI18n.Form();
    formulario.setTitle("Ingresar");
    formulario.setUsername("Código de usuario");
    formulario.setPassword("Contraseña");
    formulario.setSubmit("Ingresar");
    formulario.setForgotPassword("Olvidé mi contraseña");
    textos.setForm(formulario);

    LoginI18n.ErrorMessage mensajeError
            = new LoginI18n.ErrorMessage();
    mensajeError.setTitle("No se pudo ingresar");
    mensajeError.setMessage(
            "Código o contraseña incorrectos."
    );
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

    /*
     * Consulta al backend para comprobar
     * el código y la contraseña.
     */
    UsuarioDTO usuario
            = autenticacionClient.autenticar(
                    codigoIngresado,
                    contrasenaIngresada
            );

    /*
     * Guarda temporalmente al usuario
     * confirmado por el backend.
     */
    sesionUsuario.iniciarSesion(
            usuario,
            contrasenaIngresada
    );

    /*
     * Abre la pantalla principal.
     */
    UI.getCurrent().navigate(
            InicioView.class
    );

} catch (RestClientResponseException excepcion) {

    limpiarContrasena();

    int estado
            = excepcion.getStatusCode().value();

    if (estado == 401) {
        formularioLogin.setError(true);
    } else if (estado == 403) {
        Notification.show(
                "El usuario no tiene permiso para ingresar."
        );
    } else {
        Notification.show(
                "No se pudo iniciar sesión."
        );
    }

} catch (ResourceAccessException excepcion) {

    /*
     * Ocurre cuando el backend está apagado
     * o no se puede establecer conexión.
     */
    Notification.show(
            "No se puede conectar con el servidor."
    );

} catch (IllegalArgumentException excepcion) {

    /*
        * Ocurre si el código no comienza
        * con ADM, INV o VIG.
        */
       Notification.show(
               excepcion.getMessage()
       );

   } finally {

       formularioLogin.setEnabled(true);
   }
   }

    /*
     * Vacía solamente la contraseña cuando el backend
     * rechaza el ingreso y conserva el código escrito.
     */
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

    // Si se volvió al login teniendo una sesión iniciada,
    // cerramos esa sesión por seguridad.
    if (sesionUsuario.estaAutenticado()) {
        sesionUsuario.cerrarSesion();
    }

    // Oculta cualquier error anterior al volver al login.
    formularioLogin.setError(false);
}
    

}
