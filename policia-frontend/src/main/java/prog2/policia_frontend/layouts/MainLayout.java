package prog2.policia_frontend.layouts;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.views.InicioView;
import prog2.policia_frontend.views.LoginView;
import prog2.policia_frontend.views.MiPerfilView;

/*
 * Estructura general de todas las pantallas privadas.
 */
public class MainLayout extends AppLayout
        implements BeforeEnterObserver {

    private final SesionUsuario sesionUsuario;

    /*
     * Este texto mostrará el nombre y el rol
     * del usuario que inició sesión.
     */
    private final Span datosUsuario = new Span();

    public MainLayout(SesionUsuario sesionUsuario) {

        this.sesionUsuario = sesionUsuario;

        // Mantiene visible el menú lateral.
        setDrawerOpened(true);

        crearEncabezado();
        crearMenuLateral();
    }

    /*
     * Crea la barra superior de la aplicación.
     */
    private void crearEncabezado() {

        H2 titulo = new H2(
                "Sistema de Gestión Policial"
        );

        Button botonCerrarSesion = new Button(
                "Cerrar sesión"
        );
        //crea el boton cerrar secion, ycuando le hagan click ejecuta el metodo
        botonCerrarSesion.addClickListener(
                evento -> cerrarSesion()
        );
           
        //FILA!!!
        HorizontalLayout encabezado
                = new HorizontalLayout();
        //sus datos
        encabezado.setWidthFull();
        encabezado.setAlignItems(
                HorizontalLayout.Alignment.CENTER
        );

        /*
         * Hace que el título ocupe el espacio disponible
         * y empuja los datos del usuario hacia la derecha.
         */
        encabezado.expand(titulo);

        encabezado.add(
                titulo,
                datosUsuario,
                botonCerrarSesion
        );
        //lo deja en la parte superior
        addToNavbar(encabezado);
    }

    /*
     * Crea el menú lateral fijo.
     */
    private void crearMenuLateral() {
        //when apreten inicio ,abri inicioview
        RouterLink opcionInicio = new RouterLink(
                "Inicio",
                InicioView.class
        );
        RouterLink opcionMiPerfil = new RouterLink(
        "Mi perfil",
        MiPerfilView.class
        );

        VerticalLayout menu = new VerticalLayout(
        opcionInicio,
        opcionMiPerfil
        );
        //lo pone en lado izquierdo
        addToDrawer(menu);
    }

    /*
     * Antes de mostrar una pantalla privada,
     * comprueba que exista un usuario autenticado.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent evento) {

        if (!sesionUsuario.estaAutenticado()) {

            evento.rerouteTo(LoginView.class);
            return;
        }

        String nombre
                = sesionUsuario.getUsuario().getNombre();

        String rol
                = sesionUsuario.getUsuario().getRol();

        datosUsuario.setText(
                nombre + " - " + rol
        );
    }

    /*
     * Borra los datos de la sesión
     * y regresa al login.
     */
    private void cerrarSesion() {

        sesionUsuario.cerrarSesion();

        UI.getCurrent().navigate(
                LoginView.class
        );
    }
}