package prog2.policia_frontend.layouts;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
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

        // Estas dos opciones pertenecen a cualquier usuario autenticado.
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

        /*
         * Las opciones administrativas se muestran cuando el usuario
         * posee LECTURA. Tener ESCRITURA agregará los botones de cambio
         * dentro de cada vista, pero no reemplaza el permiso de lectura.
         */
        agregarOpcionConPermiso(
                menu,
                new RouterLink("Usuarios", UsuariosView.class),
                PermisosApp.LECTURA
        );
        agregarOpcionConPermiso(
                menu,
                new RouterLink("Roles", RolesView.class),
                PermisosApp.LECTURA
        );
        agregarOpcionConPermiso(
                menu,
                new RouterLink("Vigilantes", VigilantesView.class),
                PermisosApp.LECTURA
        );
        agregarOpcionConPermiso(
                menu,
                new RouterLink("Investigadores", InvestigadoresView.class),
                PermisosApp.LECTURA
        );
        agregarOpcionConPermiso(
                menu,
                new RouterLink("Jueces", JuecesView.class),
                PermisosApp.LECTURA
        );
        agregarOpcionConPermiso(
                menu,
                new RouterLink("Entidades bancarias", EntidadesBancariasView.class),
                PermisosApp.LECTURA
        );
        agregarOpcionConPermiso(
                menu,
                new RouterLink("Sucursales", SucursalesView.class),
                PermisosApp.LECTURA
        );
        agregarOpcionConPermiso(
                menu,
                new RouterLink("Contratos", ContratosVigilanciaView.class),
                PermisosApp.LECTURA
        );
        agregarOpcionConPermiso(
                menu,
                new RouterLink("Bandas", BandasView.class),
                PermisosApp.LECTURA
        );
        agregarOpcionConPermiso(
                menu,
                new RouterLink("Asaltantes", AsaltantesView.class),
                PermisosApp.LECTURA
        );
        agregarOpcionConPermiso(
                menu,
                new RouterLink("Asaltos", AsaltosView.class),
                PermisosApp.LECTURA
        );
        agregarOpcionConPermiso(
                menu,
                new RouterLink("Casos judiciales", CasosJudicialesView.class),
                PermisosApp.LECTURA
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
 * Agrega una opción al menú solamente cuando
 * el usuario posee el permiso requerido.
 *
 * Lo utilizaremos cuando el backend entregue
 * los permisos del usuario autenticado.
 */
    private void agregarOpcionConPermiso(
        VerticalLayout menu,
        RouterLink opcion,
        String permisoRequerido) {

                if (sesionUsuario.tienePermiso(
                     permisoRequerido)) {
        menu.add(opcion);
    }
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
