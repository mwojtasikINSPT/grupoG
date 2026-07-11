package views.submenues;

import controllers.UsuariosController;
import models.Rol;
import models.Usuario;
import views.UIHelper;
import java.util.List;

public class GestionUsuarios {
    private final UsuariosController usuariosController = new UsuariosController();

    public void mostrar(Rol rolUsuario, String usuarioLogueado) {
        int opcion;
        do {
            UIHelper.mostrarSubtitulo("GESTIÓN DE USUARIOS");
            UIHelper.imprimirMensaje("1. Listar Usuarios");
            
            if (rolUsuario == Rol.ADMINISTRADOR) {
                UIHelper.imprimirMensaje("2. Registrar nuevo Usuario\n3. Eliminar Usuario");
            }
            UIHelper.imprimirMensaje("0. Volver");

            opcion = UIHelper.leerEntero("Seleccione opción");

            if ((opcion == 2 || opcion == 3) && rolUsuario != Rol.ADMINISTRADOR) {
                UIHelper.imprimirError("Acceso denegado.");
                continue;
            }

            switch (opcion) {
                case 1 -> ejecutarListarUsuarios();
                case 2 -> ejecutarRegistrarUsuario();
                case 3 -> ejecutarEliminarUsuario(usuarioLogueado);
            }
        } while (opcion != 0);
    }

    private void ejecutarListarUsuarios() {
        UIHelper.mostrarSubtitulo("LISTADO DE USUARIOS");
        try {
            List<Usuario> lista = usuariosController.listarUsuarios();
            if (lista.isEmpty()) {
                UIHelper.imprimirMensaje("No hay usuarios registrados.");
            } else {
                for (Usuario u : lista) {
                    System.out.printf("%-25s | %-20s%n", u.getUsername(), u.obtenerRol().name());
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar();
    }

    private void ejecutarRegistrarUsuario() {
        UIHelper.mostrarSubtitulo("REGISTRO DE USUARIO");
        String user = UIHelper.leerTexto("Nombre de usuario");
        String pass = UIHelper.leerTexto("Contraseña");
        Rol rol = UIHelper.leerRol("Rol (ADMINISTRADOR, INVESTIGADOR, VIGILANTE)");
        String cod = (rol == Rol.VIGILANTE) ? UIHelper.leerTexto("Código de vigilante") : null;

        try {
            usuariosController.registrarUsuario(user, pass, rol, cod);
            UIHelper.imprimirExito("Usuario registrado con éxito.");
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar();
    }

    private void ejecutarEliminarUsuario(String usuarioLogueado) {
        UIHelper.mostrarSubtitulo("ELIMINAR USUARIO");
        String nombre = UIHelper.leerTexto("Nombre de usuario a eliminar");
        
        if (UIHelper.leerBooleano("¿Confirma la eliminación?")) {
            try {
                usuariosController.eliminarUsuario(nombre, usuarioLogueado);
                UIHelper.imprimirExito("Usuario eliminado correctamente.");
            } catch (Exception e) {
                UIHelper.imprimirError(e.getMessage());
            }
        }
        UIHelper.pausar();
    }
}