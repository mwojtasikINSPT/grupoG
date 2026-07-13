package controllers;

import models.Usuario;

//No lo estamos usando
/**
 * Controlador encargado de dirigir el flujo de navegación hacia el menú
 * correspondiente según el rol del usuario autenticado.
 */
public class MenuController {

    /**
     * Identifica el rol del usuario y ejecuta el menú asociado.
     *
     * @param usuarioLogueado El objeto {@link Usuario} que ha iniciado sesión.
     */
    public void arrancarMenuPorRol(Usuario usuarioLogueado) {
        if (usuarioLogueado == null) {
            System.out.println("[ERROR] Sesión inválida.");
            return;
        }

        switch (usuarioLogueado.obtenerRol()) {
            case ADMINISTRADOR ->
                System.out.println("\n [SISTEMA] Abriendo Menú de Administrador...");

            case INVESTIGADOR ->
                System.out.println("\n [SISTEMA] Abriendo Menú de Investigador...");

            case VIGILANTE ->
                System.out.println("\n [SISTEMA] Abriendo Menú de Vigilante...");

            default ->
                System.out.println("[ERROR] El rol del usuario es desconocido.");
        }
    }
}
