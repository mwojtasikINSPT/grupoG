package prog2.policia_frontend.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import prog2.policia_frontend.clients.AutenticacionClient;
import prog2.policia_frontend.security.SesionUsuario;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.Autocomplete;
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

    private final TextField campoCodigo
            = new TextField("Código de usuario");

    private final PasswordField campoContrasena
            = new PasswordField("Contraseña");

    private final Button botonIngresar
            = new Button("Ingresar");

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
        configurarEventos();
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

        

         // HACE QUE no me sugiera datos q ya ingrese antes 
           campoCodigo.setAutocomplete(Autocomplete.OFF);
           campoContrasena.setAutocomplete(Autocomplete.OFF);
           //Y QUE SON OBLIGATORIOS
           campoCodigo.setRequiredIndicatorVisible(true);
           campoContrasena.setRequiredIndicatorVisible(true);
        
           campoCodigo.setWidth("320px");
           campoContrasena.setWidth("320px");
           botonIngresar.setWidth("320px");

           campoCodigo.addClassName("login-campo");
           campoContrasena.addClassName("login-campo");
           botonIngresar.addClassName("login-boton");

        VerticalLayout tarjeta = new VerticalLayout(
                titulo,
                subtitulo,
                campoCodigo,
                campoContrasena,
                botonIngresar
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
 * Define qué debe ocurrir cuando
 * el usuario presiona el botón Ingresar.
 */
private void configurarEventos() {

    botonIngresar.addClickListener(//when alguien haga clic
            evento -> intentarIngresar()// inteneta ingresar 
    );
}

//INGRESO DE CAMPOS
private void intentarIngresar() {

    boolean codigoVacio // si tiene vacio o espacios 
            = campoCodigo.getValue().isBlank();

    boolean contrasenaVacia
            = campoContrasena.getValue().isBlank();

    campoCodigo.setErrorMessage(
            "Debe ingresar su código."
    );

    campoContrasena.setErrorMessage(
            "Debe ingresar su contraseña."
    );

    campoCodigo.setInvalid(codigoVacio);//es incorrecto si esta vacio
    campoContrasena.setInvalid(contrasenaVacia);//lo mismo 

    if (codigoVacio || contrasenaVacia) {
        return; //si es blanco cualquiera vuelve
    }

        String codigoIngresado
        = campoCodigo.getValue();

String contrasenaIngresada
        = campoContrasena.getValue();

botonIngresar.setEnabled(false);

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
     * Limpia los campos para que las
     * credenciales no queden visibles.
     */
    campoCodigo.clear();
    campoContrasena.clear();

    /*
     * Abre la pantalla principal.
     */
    UI.getCurrent().navigate(
            InicioView.class
    );

} catch (RestClientResponseException excepcion) {

    campoContrasena.clear();
    campoContrasena.focus();

    int estado
            = excepcion.getStatusCode().value();

    if (estado == 401) {
        Notification.show(
                "Código o contraseña incorrectos."
        );
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

       botonIngresar.setEnabled(true);
   }
   }
    
    @Override
    public void beforeEnter(BeforeEnterEvent evento) {

    // Si se volvió al login teniendo una sesión iniciada,
    // cerramos esa sesión por seguridad.
    if (sesionUsuario.estaAutenticado()) {
        sesionUsuario.cerrarSesion();
    }

    // Siempre mostramos vacíos los campos del login.
    campoCodigo.clear();
    campoContrasena.clear();
}
    

}
