package views;

import views.submenues.GestionUsuarios;
import views.submenues.GestionOperativa;
import views.submenues.GestionJudicial;
import models.Rol;
import models.Usuario;

/**
 * Clase encargada de la gestión del menú principal para usuarios con rol
 * INVESTIGADOR. Proporciona un menú interactivo en consola que permite acceder
 * a las distintas áreas de gestión del sistema: usuarios, operaciones y
 * judicial.
 *
 * Funcionalidades principales: - Gestión de usuarios: acceso al submenú para
 * listar usuarios (sin privilegios de escritura). - Gestión operativa: acceso
 * al submenú para consultar vigilantes, sucursales y contratos. - Gestión
 * judicial: acceso al submenú para consultar detenidos, bandas, asaltos y
 * jueces. - Cierre de sesión: finaliza la interacción del investigador con el
 * sistema.
 *
 * Esta clase actúa como punto de entrada para las funciones de investigación,
 * delegando la lógica específica a las clases de submenús correspondientes
 * (GestionUsuarios, GestionOperativa y GestionJudicial), con restricciones
 * propias del rol INVESTIGADOR.
 *
 * @author GrupoG
 */
public class MenuInvestigador {

    /**
     * Muestra el menú principal de investigación y gestiona la navegación hacia
     * los distintos módulos, delegando el control de permisos a cada submódulo.
     *
     * @param usuarioLogueado El objeto {@link Usuario} que ha iniciado sesión.
     */
    public void mostrarMenu(Usuario usuarioLogueado) {
        int opcion;
        do {
            UIHelper.mostrarTitulo("Menú de Investigador - Hola, " + usuarioLogueado.getUsername());
            UIHelper.imprimirMensaje("1. Gestión de Usuarios\n2. Gestión Operativa\n3. Gestión Judicial\n0. Salir");

            opcion = UIHelper.leerEntero("Seleccione una opción");

            switch (opcion) {
                // Al pasar ROL.INVESTIGADOR, las clases ya bloquean las opciones de escritura
                case 1 ->
                    new GestionUsuarios().mostrar(Rol.INVESTIGADOR, usuarioLogueado);
                case 2 ->
                    new GestionOperativa().mostrar(Rol.INVESTIGADOR);
                case 3 ->
                    new GestionJudicial().mostrar(Rol.INVESTIGADOR);
                case 0 ->
                    UIHelper.imprimirMensaje("Cerrando sesión...");
                default ->
                    UIHelper.imprimirError("Opción no válida.");
            }
        } while (opcion != 0);
    }
}
