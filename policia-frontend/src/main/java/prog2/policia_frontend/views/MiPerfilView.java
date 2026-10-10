package prog2.policia_frontend.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import prog2.policia_frontend.dtos.UsuarioDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.SesionUsuario;

/*
 * Pantalla donde el usuario puede consultar
 * sus propios datos.
 */
@Route(value = "mi-perfil", layout = MainLayout.class)
@PageTitle("Mi perfil | Sistema Policial")
public class MiPerfilView extends VerticalLayout
        implements BeforeEnterObserver {

    private final SesionUsuario sesionUsuario;

    private final Paragraph datoCodigo = new Paragraph();
    private final Paragraph datoNombre = new Paragraph();
    private final Paragraph datoRol = new Paragraph();
    private final Paragraph datoEstado = new Paragraph();

    public MiPerfilView(SesionUsuario sesionUsuario) {

        this.sesionUsuario = sesionUsuario;

        H2 titulo = new H2("Mi perfil");

        add(
                titulo,
                datoCodigo,
                datoNombre,
                datoRol,
                datoEstado
        );
    }

    /*
     * Antes de mostrar los datos,
     * comprueba que exista un usuario conectado.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent evento) {

        if (!sesionUsuario.estaAutenticado()) {

            evento.rerouteTo(LoginView.class);
            return;
        }

        mostrarDatos();
    }

    /*
     * Obtiene de la sesión los datos del usuario
     * que inició sesión y los muestra en pantalla.
     */
    private void mostrarDatos() {

        UsuarioDTO usuario
                = sesionUsuario.getUsuario();

        datoCodigo.setText(
                "Código: " + usuario.getCodigo()
        );

        datoNombre.setText(
                "Nombre: " + usuario.getNombre()
        );

        datoRol.setText(
                "Rol: " + usuario.getRol()
        );

        String estado;

        if (usuario.getActivo() == null) {
            estado = "No informado";
        } else if (usuario.getActivo()) {
            estado = "Activo";
        } else {
            estado = "Inactivo";
        }

        datoEstado.setText(
                "Estado: " + estado
        );
    }
}