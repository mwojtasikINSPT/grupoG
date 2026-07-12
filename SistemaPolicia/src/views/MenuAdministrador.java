package views;

import views.submenues.GestionOperativa;
import views.submenues.GestionUsuarios;
import models.Rol;
import models.Usuario;
import views.submenues.GestionJudicial;

/**
 * Clase encargada de la gestión del menú principal para usuarios con rol ADMINISTRADOR.
 * Proporciona un menú interactivo en consola que permite acceder a las distintas
 * áreas de gestión del sistema: usuarios, operaciones y judicial.
 *
 * Funcionalidades principales:
 * - Gestión de usuarios: acceso al submenú para listar, registrar y eliminar cuentas.
 * - Gestión operativa: acceso al submenú para listar y registrar vigilantes, sucursales y contratos.
 * - Gestión judicial: acceso al submenú para consultar detenidos, bandas, asaltos y jueces.
 * - Cierre de sesión: finaliza la interacción del administrador con el sistema.
 *
 * Esta clase actúa como punto de entrada para las funciones administrativas,
 * delegando la lógica específica a las clases de submenús correspondientes
 * (GestionUsuarios, GestionOperativa y GestionJudicial).
 *
 * @author GrupoG
 */

public class MenuAdministrador {

    /**
     * Muestra el menú principal de administración y gestiona la navegación 
     * hacia las distintas submódulos del sistema.
     *
     * @param usuarioLogueado El objeto {@link Usuario} que ha iniciado sesión.
     */
    public void mostrarMenu(Usuario usuarioLogueado) {
        int opcion;

        do {
            UIHelper.mostrarTitulo("MENÚ DE ADMIN - Hola," + usuarioLogueado.getUsername());

            UIHelper.imprimirMensaje("1. Gestión de Usuarios\n"
                    + "2. Gestión Operativa\n"
                    + "3. Gestión Judicial\n"
                    + "0. Cerrar Sesión / Salir\n"
                    + "----------------------------------------");

            opcion = UIHelper.leerEntero("Seleccione una opción");

            switch (opcion) {
                case 1 ->
                    new GestionUsuarios().mostrar(Rol.ADMINISTRADOR, usuarioLogueado);
                case 2 ->
                    new GestionOperativa().mostrar(Rol.ADMINISTRADOR);
                case 3 ->
                    new GestionJudicial().mostrar(Rol.ADMINISTRADOR);
                case 0 ->
                    UIHelper.imprimirMensaje("\n[SISTEMA] Cerrando sesión...");
                default ->
                    UIHelper.imprimirError("Opción no válida.");
            }
        } while (opcion != 0);
    }
}
