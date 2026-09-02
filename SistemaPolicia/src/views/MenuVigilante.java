package views;

import models.Usuario;
import models.UsuarioVigilante;
import views.submenues.ConsultaVigilante;

/**
 * Presenta las opciones disponibles para una cuenta con rol de vigilante.
 *
 * La vista utiliza el código del vigilante asociado y delega sus consultas a
 * {@link ConsultaVigilante}.
 *
 * @author GrupoG
 */
public class MenuVigilante implements VistaMenu {

    /**
     * Muestra las consultas correspondientes al vigilante autenticado.
     *
     * @param usuarioLogueado usuario que inició sesión
     */
    @Override
    public void mostrarMenu(Usuario usuarioLogueado) {
        if (!(usuarioLogueado
                instanceof UsuarioVigilante vigilanteLogueado)) {

            UIHelper.imprimirError(
                    "Perfil de usuario incorrecto."
            );
            return;
        }

        String codigoVigilante =
                vigilanteLogueado.getCodigoVigilante();

        if (codigoVigilante == null
                || codigoVigilante.trim().isEmpty()) {

            UIHelper.imprimirError(
                    "La cuenta no tiene un vigilante asociado."
            );
            return;
        }

        new ConsultaVigilante().mostrar(codigoVigilante);
    }
}