package prog2.policia_frontend.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.SesionUsuario;

/**
 * Pantalla principal que recibe al usuario autenticado.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Inicio | Sistema Policial")
public class InicioView extends VerticalLayout
        implements BeforeEnterObserver, BeforeLeaveObserver {

    private final SesionUsuario sesionUsuario;
    private final H1 titulo = new H1("Sistema Policial");
    private final Paragraph datosUsuario = new Paragraph();

    public InicioView(SesionUsuario sesionUsuario) {

        this.sesionUsuario = sesionUsuario;

        setSizeFull();
        setPadding(true);
        setSpacing(false);

        add(crearPresentacion());
    }

    /**
     * Crea la presentación visual utilizando componentes Vaadin y las mismas
     * imágenes institucionales del acceso al sistema.
     */
    private Div crearPresentacion() {

        Div presentacion = new Div();

        presentacion.getStyle()
                .set("position", "relative")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "100%")
                .set("min-height", "clamp(380px, 65vh, 650px)")
                .set("box-sizing", "border-box")
                .set("overflow", "hidden")
                .set("border-radius", "24px")
                .set("border", "1px solid rgba(84, 176, 255, 0.35)")
                .set("background-image",
                        "linear-gradient(110deg, "
                        + "rgba(3, 12, 23, 0.88), "
                        + "rgba(5, 27, 49, 0.62)), "
                        + "url('/images/patrullero-ciudad.jpg')")
                .set("background-position", "center")
                .set("background-size", "cover")
                .set("box-shadow", "0 20px 55px rgba(0, 0, 0, 0.38)");

        Image asisa = crearImagenDecorativa(
                "images/asisa-policia.png",
                "Asisa con chaleco policial"
        );
        asisa.getStyle()
                .set("left", "clamp(-20px, 1vw, 18px)")
                .set("width", "clamp(150px, 21vw, 315px)");

        Image copo = crearImagenDecorativa(
                "images/copo-policia.png",
                "Copo con chaleco policial"
        );
        copo.getStyle()
                .set("right", "clamp(8px, 3vw, 45px)")
                .set("width", "clamp(105px, 13vw, 190px)");

        Div bienvenida = new Div(titulo, datosUsuario);
        bienvenida.getStyle()
                .set("position", "relative")
                .set("z-index", "2")
                .set("width", "min(560px, 72%)")
                .set("box-sizing", "border-box")
                .set("padding", "clamp(1.4rem, 3vw, 2.5rem)")
                .set("text-align", "center")
                .set("color", "white")
                .set("border-radius", "22px")
                .set("border", "1px solid rgba(112, 192, 255, 0.42)")
                .set("background", "rgba(5, 20, 36, 0.78)")
                .set("backdrop-filter", "blur(8px)")
                .set("box-shadow", "0 18px 45px rgba(0, 0, 0, 0.42)");

        titulo.getStyle()
                .set("margin", "0")
                .set("font-size", "clamp(1.8rem, 4vw, 3rem)")
                .set("line-height", "1.15")
                .set("text-shadow", "0 3px 18px rgba(0, 102, 255, 0.45)");

        datosUsuario.getStyle()
                .set("display", "inline-block")
                .set("margin", "1rem 0 0")
                .set("padding", "0.45rem 1rem")
                .set("color", "#b9e0ff")
                .set("font-weight", "600")
                .set("letter-spacing", "0.04em")
                .set("border-radius", "999px")
                .set("background", "rgba(17, 105, 171, 0.28)");

        presentacion.add(asisa, bienvenida, copo);

        return presentacion;
    }

    private Image crearImagenDecorativa(
            String ruta,
            String textoAlternativo) {

        Image imagen = new Image(ruta, textoAlternativo);

        imagen.getStyle()
                .set("position", "absolute")
                .set("bottom", "0")
                .set("z-index", "1")
                .set("max-height", "58%")
                .set("object-fit", "contain")
                .set("pointer-events", "none")
                .set("filter", "drop-shadow(0 16px 22px rgba(0, 0, 0, 0.6))");

        return imagen;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {

        if (!sesionUsuario.estaAutenticado()) {
            evento.rerouteTo(LoginView.class);
            return;
        }

        mostrarUsuario();
    }

    private void mostrarUsuario() {

        String nombre = sesionUsuario.getUsuario().getNombre();
        String rol = sesionUsuario.getUsuario().getRol();

        titulo.setText("Bienvenido, " + nombre);
        datosUsuario.setText("Rol: " + rol);
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent evento) {

        if ("login".equals(evento.getLocation().getPath())) {
            sesionUsuario.cerrarSesion();
        }
    }
}
