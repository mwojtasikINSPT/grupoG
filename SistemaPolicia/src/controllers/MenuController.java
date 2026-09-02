package controllers;

import java.util.EnumMap;
import java.util.Map;
import models.Rol;
import models.Usuario;
import views.MenuAdministrador;
import views.MenuInvestigador;
import views.MenuVigilante;
import views.VistaMenu;

/**
 * Selecciona y ejecuta la vista correspondiente al rol del usuario.
 *
 * Las vistas se almacenan mediante la interfaz {@link VistaMenu}, evitando
 * que la navegación dependa de una implementación concreta.
 *
 * @author GrupoG
 */
public class MenuController {

    private final Map<Rol, VistaMenu> vistas;

    /**
     * Crea el controlador y registra las vistas disponibles inicialmente.
     */
    public MenuController() {
        vistas = new EnumMap<>(Rol.class);

        registrarVista(Rol.ADMINISTRADOR, new MenuAdministrador());
        registrarVista(Rol.INVESTIGADOR, new MenuInvestigador());
        registrarVista(Rol.VIGILANTE, new MenuVigilante());
    }

    /**
     * Registra o reemplaza la vista asociada con un rol.
     *
     * Este método permite incorporar otra implementación de {@link VistaMenu}
     * sin modificar la lógica que selecciona y ejecuta las vistas.
     *
     * @param rol rol asociado con la vista
     * @param vista implementación que mostrará el menú
     * @throws IllegalArgumentException si el rol o la vista son nulos
     */
    public final void registrarVista(Rol rol, VistaMenu vista) {
        if (rol == null || vista == null) {
            throw new IllegalArgumentException(
                    "El rol y la vista son obligatorios."
            );
        }

        vistas.put(rol, vista);
    }

    /**
     * Ejecuta la vista correspondiente al rol del usuario autenticado.
     *
     * @param usuarioLogueado usuario que inició sesión
     * @throws IllegalArgumentException si el usuario es nulo
     * @throws IllegalStateException si no existe una vista para su rol
     */
    public void arrancarMenuPorRol(Usuario usuarioLogueado) {
        if (usuarioLogueado == null) {
            throw new IllegalArgumentException(
                    "El usuario autenticado es obligatorio."
            );
        }

        VistaMenu vista = vistas.get(usuarioLogueado.obtenerRol());

        if (vista == null) {
            throw new IllegalStateException(
                    "No existe una vista para el rol "
                    + usuarioLogueado.obtenerRol()
            );
        }

        vista.mostrarMenu(usuarioLogueado);
    }
}