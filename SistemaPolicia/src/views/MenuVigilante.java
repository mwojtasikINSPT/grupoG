package views;

import views.submenues.ConsultaVigilante;
import models.Usuario;
import models.UsuarioVigilante;

/**
 * Clase encargada de la gestión del menú principal para usuarios con rol
 * VIGILANTE. Proporciona un acceso directo al submenú de consultas específicas
 * para vigilantes, permitiendo visualizar datos personales y contratos
 * asociados.
 *
 * Funcionalidades principales: - Validar que el usuario logueado sea de tipo
 * {@link UsuarioVigilante}. - Delegar la consulta de datos y contratos a la
 * clase {@link ConsultaVigilante}. - Informar errores en caso de que el perfil
 * de usuario no corresponda.
 *
 * Esta clase actúa como punto de entrada para los vigilantes dentro del
 * sistema, garantizando que solo accedan a la información relevante para su
 * rol.
 *
 * @author GrupoG
 */
public class MenuVigilante {

    /**
     * Valida el perfil del usuario logueado y delega la navegación al submenú
     * de consultas específicas para el vigilante.
     *
     * @param usuarioLogueado El objeto {@link Usuario} que ha iniciado sesión.
     */
    public void mostrarMenu(Usuario usuarioLogueado) {
        if (!(usuarioLogueado instanceof UsuarioVigilante vigilanteLogueado)) {
            UIHelper.imprimirError("Perfil de usuario incorrecto.");
            return;
        }

        // Delego la consulta a la nueva clase del paquete submenues
        new ConsultaVigilante().mostrar(vigilanteLogueado.getVigilante().getCodigo());
    }
}
