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
                .set("font-size", "18px")
                .set("margin", "0");

        addToNavbar(menuToggle, titulo);

        RouterLink inicio = new RouterLink("Inicio", InicioView.class);
        RouterLink vigilantes = new RouterLink(
                "Vigilantes", VigilantesView.class);
        RouterLink entidades = new RouterLink(
                "Entidades bancarias", EntidadesBancariasView.class);
        RouterLink sucursales = new RouterLink(
                "Sucursales", SucursalesView.class);
        RouterLink bandas = new RouterLink("Bandas", BandasView.class);
        RouterLink asaltantes = new RouterLink("Asaltantes", AsaltantesView.class);
        RouterLink asaltos = new RouterLink("Asaltos", AsaltosView.class);
        RouterLink contratos = new RouterLink(
                "Contratos de vigilancia", ContratosVigilanciaView.class);
        RouterLink jueces = new RouterLink("Jueces", JuecesView.class);
        RouterLink casos = new RouterLink(
                "Casos judiciales", CasosJudicialesView.class);
        RouterLink usuarios = new RouterLink("Usuarios", UsuariosView.class);

        Nav navegacion = new Nav(inicio, entidades, sucursales, vigilantes,
                contratos, bandas, asaltantes, asaltos, jueces, casos, usuarios);
        navegacion.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "8px")
                .set("padding", "16px");

        addToDrawer(navegacion);
    }
}
