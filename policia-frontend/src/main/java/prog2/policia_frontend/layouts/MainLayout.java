package prog2.policia_frontend.layouts;

import java.util.Set;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.component.applayout.DrawerToggle;

import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.security.PermisosInterfaz;
import prog2.policia_frontend.utils.NotificacionUtils;
import prog2.policia_frontend.views.AsaltantesView;
import prog2.policia_frontend.views.AsaltosView;
import prog2.policia_frontend.views.BandasView;
import prog2.policia_frontend.views.CasosJudicialesView;
import prog2.policia_frontend.views.ContratosVigilanciaView;
import prog2.policia_frontend.views.EntidadesBancariasView;
import prog2.policia_frontend.views.InicioView;
import prog2.policia_frontend.views.JuecesView;
import prog2.policia_frontend.views.LoginView;
import prog2.policia_frontend.views.MiPerfilView;
import prog2.policia_frontend.views.MisContratosView;
import prog2.policia_frontend.views.RolesView;
import prog2.policia_frontend.views.SucursalesView;
import prog2.policia_frontend.views.UsuariosView;

// Estructura general de todas las pantallas privadas.
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    // Vistas que requieren autorización.
    private static final Set<Class<?>> VISTAS_ADMINISTRATIVAS = Set.of(UsuariosView.class, RolesView.class, JuecesView.class, EntidadesBancariasView.class, SucursalesView.class, ContratosVigilanciaView.class, MisContratosView.class, BandasView.class, AsaltantesView.class, AsaltosView.class, CasosJudicialesView.class);
    private final SesionUsuario sesionUsuario;
    private final PermisosInterfaz permisosInterfaz;

    // Este texto mostrará el nombre y el rol del usuario que inició sesión.
    private final Span datosUsuario = new Span();

    public MainLayout(SesionUsuario sesionUsuario, PermisosInterfaz permisosInterfaz) {
        this.sesionUsuario = sesionUsuario;
        this.permisosInterfaz = permisosInterfaz;

        // Configura la barra de navegación.
        setPrimarySection(AppLayout.Section.NAVBAR);
        crearEncabezado();
        crearMenuLateral();
    }

    // Crea la barra superior de la aplicación.
    private void crearEncabezado() {
        H2 titulo = new H2("Sistema de Gestión Policial");
        Button botonCerrarSesion = new Button("Cerrar sesión");
        botonCerrarSesion.addClickListener(evento -> cerrarSesion());
        HorizontalLayout encabezado = new HorizontalLayout();
        encabezado.setWidthFull();
        encabezado.setAlignItems(HorizontalLayout.Alignment.CENTER);

        // Hace que el título ocupe el espacio disponible y empuja los datos del usuario hacia la derecha.
        DrawerToggle botonMenu = new DrawerToggle();
        botonMenu.setAriaLabel("Mostrar u ocultar el menú");
        botonMenu.setTooltipText("Mostrar u ocultar el menú");

        // Quita el margen propio del H2 para que el título quede pegado al botón
        titulo.getStyle().set("margin", "0");
        encabezado.expand(titulo);
        encabezado.add(botonMenu, titulo, datosUsuario, botonCerrarSesion);
        addToNavbar(encabezado);
    }

    // Crea el menú lateral fijo.
    private void crearMenuLateral() {
        // Crea el menú general.
        SideNav menuGeneral = new SideNav("General");
        menuGeneral.addItem(new SideNavItem("Inicio", InicioView.class, VaadinIcon.HOME.create()),
                new SideNavItem("Mi perfil", MiPerfilView.class, VaadinIcon.USER.create()));
        VerticalLayout menu = new VerticalLayout(menuGeneral);
        menu.setPadding(false);
        menu.setSpacing(false);
        menu.setWidthFull();

        // Agrega secciones según los permisos de la sesión.
        SideNav menuAdministracion = new SideNav("Administración");
        boolean mostrarAdministracion = false;
        if (permisosInterfaz.puedeAccederVista(UsuariosView.class)) {
            menuAdministracion.addItem(new SideNavItem("Usuarios", UsuariosView.class, VaadinIcon.USERS.create()));
            mostrarAdministracion = true;
        }
        if (permisosInterfaz.puedeAccederVista(RolesView.class)) {
            menuAdministracion.addItem(new SideNavItem("Roles", RolesView.class, VaadinIcon.KEY.create()));
            mostrarAdministracion = true;
        }
        SideNav menuPersonal = new SideNav("Personal");
        boolean mostrarPersonal = false;
        if (permisosInterfaz.puedeAccederVista(JuecesView.class)) {
            menuPersonal.addItem(new SideNavItem("Jueces", JuecesView.class, VaadinIcon.GAVEL.create()));
            mostrarPersonal = true;
        }
        SideNav menuBancos = new SideNav("Actividad bancaria");
        boolean mostrarBancos = false;
        if (permisosInterfaz.puedeAccederVista(EntidadesBancariasView.class)) {
            menuBancos.addItem(new SideNavItem("Entidades bancarias", EntidadesBancariasView.class, VaadinIcon.BUILDING.create()));
            mostrarBancos = true;
        }
        if (permisosInterfaz.puedeAccederVista(SucursalesView.class)) {
            menuBancos.addItem(new SideNavItem("Sucursales", SucursalesView.class, VaadinIcon.BRIEFCASE.create()));
            mostrarBancos = true;
        }
        if (permisosInterfaz.puedeAccederVista(ContratosVigilanciaView.class)) {
            menuBancos.addItem(new SideNavItem("Contratos", ContratosVigilanciaView.class, VaadinIcon.FILE_TEXT.create()));
            mostrarBancos = true;
        } else if (permisosInterfaz.puedeAccederVista(MisContratosView.class)) {
            menuBancos.addItem(new SideNavItem("Mis contratos", MisContratosView.class, VaadinIcon.FILE_TEXT.create()));
            mostrarBancos = true;
        }
        SideNav menuInvestigacion = new SideNav("Investigación policial");
        boolean mostrarInvestigacion = false;
        if (permisosInterfaz.puedeAccederVista(BandasView.class)) {
            menuInvestigacion.addItem(new SideNavItem("Bandas", BandasView.class, VaadinIcon.GROUP.create()));
            mostrarInvestigacion = true;
        }
        if (permisosInterfaz.puedeAccederVista(AsaltantesView.class)) {
            menuInvestigacion.addItem(new SideNavItem("Asaltantes", AsaltantesView.class, VaadinIcon.WARNING.create()));
            mostrarInvestigacion = true;
        }
        if (permisosInterfaz.puedeAccederVista(AsaltosView.class)) {
            menuInvestigacion.addItem(new SideNavItem("Asaltos", AsaltosView.class, VaadinIcon.SAFE.create()));
            mostrarInvestigacion = true;
        }
        if (permisosInterfaz.puedeAccederVista(CasosJudicialesView.class)) {
            menuInvestigacion.addItem(new SideNavItem("Casos judiciales", CasosJudicialesView.class, VaadinIcon.SCALE.create()));
            mostrarInvestigacion = true;
        }
        if (mostrarAdministracion) {
            menu.add(menuAdministracion);
        }
        if (mostrarPersonal) {
            menu.add(menuPersonal);
        }
        if (mostrarBancos) {
            menu.add(menuBancos);
        }
        if (mostrarInvestigacion) {
            menu.add(menuInvestigacion);
        }

        // Permite desplazar el menú lateral.
        Scroller desplazamiento = new Scroller(menu);
        desplazamiento.setSizeFull();
        addToDrawer(desplazamiento);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        if (!sesionUsuario.estaAutenticado()) {
            evento.rerouteTo(LoginView.class);
            return;
        }
        
        // También controla el ingreso manual por URL. Ocultar un enlace mejora la interfaz, pero por sí solo no impide la navegación.
        if (VISTAS_ADMINISTRATIVAS.contains(evento.getNavigationTarget()) && !permisosInterfaz.puedeAccederVista(evento.getNavigationTarget())) {
            NotificacionUtils.mostrarAdvertencia("No tiene permiso para ingresar a esta sección.");
            evento.rerouteTo(InicioView.class);
            return;
        }
        String nombre = sesionUsuario.getUsuario().getNombre();
        String rol = sesionUsuario.getUsuario().getRol();
        datosUsuario.setText(nombre + " - " + rol);
    }

    // Borra los datos de la sesión y regresa al login.
    private void cerrarSesion() {
        sesionUsuario.cerrarSesion();
        UI.getCurrent().getSession().close();
        UI.getCurrent().getPage().setLocation("/login");
    }
}
