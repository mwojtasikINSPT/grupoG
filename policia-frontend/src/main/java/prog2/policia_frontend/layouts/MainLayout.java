package prog2.policia_frontend.layouts;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import java.util.Set;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.views.AsaltantesView;
import prog2.policia_frontend.views.AsaltosView;
import prog2.policia_frontend.views.BandasView;
import prog2.policia_frontend.views.CasosJudicialesView;
import prog2.policia_frontend.views.ContratosVigilanciaView;
import prog2.policia_frontend.views.EntidadesBancariasView;
import prog2.policia_frontend.views.InicioView;
import prog2.policia_frontend.views.InvestigadoresView;
import prog2.policia_frontend.views.JuecesView;
import prog2.policia_frontend.views.LoginView;
import prog2.policia_frontend.views.MiPerfilView;
import prog2.policia_frontend.views.RolesView;
import prog2.policia_frontend.views.SucursalesView;
import prog2.policia_frontend.views.UsuariosView;
import prog2.policia_frontend.views.VigilantesView;

/*
 * Estructura general de todas las pantallas privadas.
 */
public class MainLayout extends AppLayout
        implements BeforeEnterObserver {

    /*
     * Pantallas que muestran información administrativa general.
     * Para ingresar en ellas no alcanza con conocer la dirección:
     * la sesión debe poseer el permiso LECTURA.
     */
    private static final Set<Class<?>> VISTAS_ADMINISTRATIVAS
            = Set.of(
                    UsuariosView.class,
                    RolesView.class,
                    VigilantesView.class,
                    InvestigadoresView.class,
                    JuecesView.class,
                    EntidadesBancariasView.class,
                    SucursalesView.class,
                    ContratosVigilanciaView.class,
                    BandasView.class,
                    AsaltantesView.class,
                    AsaltosView.class,
                    CasosJudicialesView.class
            );

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

        /*
         * SideNav es el menú lateral oficial de Vaadin. Resalta
         * automáticamente la opción correspondiente a la vista actual.
         */
        SideNav menuGeneral = new SideNav("General");

        menuGeneral.addItem(
                new SideNavItem(
                        "Inicio",
                        InicioView.class,
                        VaadinIcon.HOME.create()
                ),
                new SideNavItem(
                        "Mi perfil",
                        MiPerfilView.class,
                        VaadinIcon.USER.create()
                )
        );

        VerticalLayout menu = new VerticalLayout(menuGeneral);
        menu.setPadding(false);
        menu.setSpacing(false);
        menu.setWidthFull();

        /*
         * Las secciones administrativas solo se agregan cuando existe
         * permiso de lectura. Los nuevos roles funcionan igual porque
         * la decisión depende del permiso, no del nombre del rol.
         */
        if (sesionUsuario.tienePermiso(PermisosApp.LECTURA)) {

            SideNav menuAdministracion
                    = new SideNav("Administración");

            menuAdministracion.addItem(
                    new SideNavItem(
                            "Usuarios",
                            UsuariosView.class,
                            VaadinIcon.USERS.create()
                    ),
                    new SideNavItem(
                            "Roles",
                            RolesView.class,
                            VaadinIcon.KEY.create()
                    )
            );

            SideNav menuPersonal = new SideNav("Personal");

            menuPersonal.addItem(
                    new SideNavItem(
                            "Vigilantes",
                            VigilantesView.class,
                            VaadinIcon.SHIELD.create()
                    ),
                    new SideNavItem(
                            "Investigadores",
                            InvestigadoresView.class,
                            VaadinIcon.SEARCH.create()
                    ),
                    new SideNavItem(
                            "Jueces",
                            JuecesView.class,
                            VaadinIcon.GAVEL.create()
                    )
            );

            SideNav menuBancos = new SideNav("Actividad bancaria");

            menuBancos.addItem(
                    new SideNavItem(
                            "Entidades bancarias",
                            EntidadesBancariasView.class,
                            VaadinIcon.BUILDING.create()
                    ),
                    new SideNavItem(
                            "Sucursales",
                            SucursalesView.class,
                            VaadinIcon.BRIEFCASE.create()
                    ),
                    new SideNavItem(
                            "Contratos",
                            ContratosVigilanciaView.class,
                            VaadinIcon.FILE_TEXT.create()
                    )
            );

            SideNav menuInvestigacion
                    = new SideNav("Investigación policial");

            menuInvestigacion.addItem(
                    new SideNavItem(
                            "Bandas",
                            BandasView.class,
                            VaadinIcon.GROUP.create()
                    ),
                    new SideNavItem(
                            "Asaltantes",
                            AsaltantesView.class,
                            VaadinIcon.WARNING.create()
                    ),
                    new SideNavItem(
                            "Asaltos",
                            AsaltosView.class,
                            VaadinIcon.CAR.create()
                    ),
                    new SideNavItem(
                            "Casos judiciales",
                            CasosJudicialesView.class,
                            VaadinIcon.SCALE.create()
                    )
            );

            menu.add(
                    menuAdministracion,
                    menuPersonal,
                    menuBancos,
                    menuInvestigacion
            );
        }

        /*
         * Scroller también es un componente oficial de Vaadin y permite
         * recorrer el menú cuando la pantalla no tiene suficiente altura.
         */
        Scroller desplazamiento = new Scroller(menu);
        desplazamiento.setSizeFull();

        addToDrawer(desplazamiento);
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

        /*
         * También controla el ingreso manual por URL. Ocultar un enlace
         * mejora la interfaz, pero por sí solo no impide la navegación.
         */
        if (VISTAS_ADMINISTRATIVAS.contains(
                evento.getNavigationTarget()
        ) && !sesionUsuario.tienePermiso(
                PermisosApp.LECTURA
        )) {

            Notification.show(
                    "No tiene permiso para ingresar a esta sección."
            );

            evento.rerouteTo(InicioView.class);
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
