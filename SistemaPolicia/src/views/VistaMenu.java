
package views;

import models.Usuario;

/**
 * Define el comportamiento común de los menús asociados a los distintos
 * tipos de usuario.
 *
 * Cada implementación decide qué opciones mostrar, mientras que el resto
 * del sistema trabaja únicamente con esta interfaz.
 *
 * @author GrupoG
 */
public interface VistaMenu {

    /**
     * Muestra el menú correspondiente al usuario autenticado.
     *
     * @param usuario usuario que inició sesión
     */
    void mostrarMenu(Usuario usuario);
}