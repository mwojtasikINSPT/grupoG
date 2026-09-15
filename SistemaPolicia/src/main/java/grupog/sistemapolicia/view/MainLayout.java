package grupog.sistemapolicia.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

    public MainLayout() {
        DrawerToggle menuToggle = new DrawerToggle();

        H1 titulo = new H1("Sistema Policial");
        titulo.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        addToNavbar(menuToggle, titulo);

        RouterLink inicio = new RouterLink("Inicio", InicioView.class);
        RouterLink vigilantes = new RouterLink(
                "Vigilantes", VigilantesView.class);

        Nav navegacion = new Nav(inicio, vigilantes);
        navegacion.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "var(--lumo-space-s)")
                .set("padding", "var(--lumo-space-m)");

        addToDrawer(navegacion);
    }
}
