
package prog2.policia_frontend.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import prog2.policia_frontend.security.SesionUsuario;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import prog2.policia_frontend.layouts.MainLayout;


//la dir principal mostrara iniciio
@Route(value = "", layout = MainLayout.class)
@PageTitle("Inicio | Sistema Policial")
public class InicioView extends VerticalLayout
        implements BeforeEnterObserver,BeforeLeaveObserver {

    private final SesionUsuario sesionUsuario;

    private final H1 titulo
            = new H1("Sistema Policial");

    private final Paragraph datosUsuario
            = new Paragraph();

    public InicioView(SesionUsuario sesionUsuario) {

        this.sesionUsuario = sesionUsuario;

        add(titulo, datosUsuario);
    }

    /*
     * Se ejecuta antes de permitir
     * que el usuario vea esta pantalla.
     */
    @Override
    public void beforeEnter(
            BeforeEnterEvent evento) {

        if (!sesionUsuario.estaAutenticado()) {
            evento.rerouteTo(LoginView.class);
            return;
        }

        mostrarUsuario();
    }

    /*
     * Muestra el nombre y el rol
     * del usuario que inició sesión.
     */
    private void mostrarUsuario() {

        String nombre
                = sesionUsuario.getUsuario().getNombre();

        String rol
                = sesionUsuario.getUsuario().getRol();

        titulo.setText(
                "Bienvenido, " + nombre
        );

        datosUsuario.setText(
                "Rol: " + rol
        );
    }
        
    @Override
    public void beforeLeave(BeforeLeaveEvent evento) {

    // Si el usuario vuelve desde Inicio hacia el Login,
    // cerramos la sesión para impedir que pueda avanzar nuevamente.
    if ("login".equals(evento.getLocation().getPath())) {
        sesionUsuario.cerrarSesion();
    }
}
}