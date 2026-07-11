package views;

import views.submenues.ConsultaVigilante;
import models.Usuario;
import models.UsuarioVigilante;

public class MenuVigilante {

    public void mostrarMenu(Usuario usuarioLogueado) {
        if (!(usuarioLogueado instanceof UsuarioVigilante vigilanteLogueado)) {
            UIHelper.imprimirError("Perfil de usuario incorrecto.");
            return;
        }

        // Delego la consulta a la nueva clase del paquete submenues
        new ConsultaVigilante().mostrar(vigilanteLogueado.getVigilante().getCodigo());
    }
}