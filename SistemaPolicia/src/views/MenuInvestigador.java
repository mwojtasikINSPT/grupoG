package views;

import models.Rol;
import models.Usuario;
import views.submenues.GestionJudicial;
import views.submenues.GestionOperativa;
import views.submenues.GestionUsuarios;

/**
 * Presenta las opciones disponibles para un usuario investigador.
 *
 * Permite consultar usuarios, información operativa y asuntos judiciales,
 * respetando los permisos asignados al rol investigador.
 *
 * @author GrupoG
 */
public class MenuInvestigador implements VistaMenu {

    /**
     * Muestra el menú de investigación hasta que el usuario cierra la sesión.
     *
     * @param usuarioLogueado investigador que inició sesión
     */
    @Override
    public void mostrarMenu(Usuario usuarioLogueado) {
        int opcion;

        do {
            UIHelper.mostrarTitulo(
                    "Menú de Investigador - Hola, "
                    + usuarioLogueado.getUsername()
            );

            UIHelper.imprimirMensaje(
                    "1. Gestión de Usuarios\n"
                    + "2. Gestión Operativa\n"
                    + "3. Gestión Judicial\n"
                    + "0. Salir"
            );

            opcion = UIHelper.leerEntero("Seleccione una opción");

            switch (opcion) {
                case 1 ->
                    new GestionUsuarios()
                            .mostrar(Rol.INVESTIGADOR, usuarioLogueado);

                case 2 ->
                    new GestionOperativa()
                            .mostrar(Rol.INVESTIGADOR);

                case 3 ->
                    new GestionJudicial()
                            .mostrar(Rol.INVESTIGADOR);

                case 0 ->
                    UIHelper.imprimirMensaje("Cerrando sesión...");

                default ->
                    UIHelper.imprimirError("Opción no válida.");
            }
        } while (opcion != 0);
    }
}