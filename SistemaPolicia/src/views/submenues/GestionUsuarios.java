package views.submenues;

import controllers.UsuariosController;
import models.Rol;
import models.Usuario;
import views.UIHelper;
import java.util.List;

/**
 * Clase encargada de la gestión de cuentas de usuario dentro del sistema.
 * Proporciona un menú interactivo en consola que permite listar, registrar
 * y eliminar usuarios, con acceso restringido según el rol del usuario activo.
 *
 * Funcionalidades principales:
 * - Listar usuarios existentes con sus roles.
 * - Registrar nuevos usuarios con credenciales y rol asignado.
 * - Eliminar usuarios, validando privilegios y confirmación del usuario activo.
 *
 * Esta clase actúa como interfaz entre la capa de presentación (UIHelper)
 * y el controlador de usuarios (UsuariosController), garantizando que las
 * operaciones se realicen de acuerdo a los privilegios del rol.
 *
 * @author GrupoG
 */
public class GestionUsuarios {

    
    //Ver - Inyectar dependencias en lugar de instanciar controladores cada vez
    private final UsuariosController usuariosController = new UsuariosController();
    // Variable de instancia para el usuario activo
    private Usuario usuarioActual;

    /**
     * Muestra el menú principal de gestión de usuarios y procesa la navegación.
     *
     * @param rolUsuario El rol del usuario que está utilizando el sistema.
     * @param usuarioLogueado El objeto {@link Usuario} que inició sesión.
     */
    public void mostrar(Rol rolUsuario, Usuario usuarioLogueado) {
        int opcion;

        // Asigno el objeto a nuestra variable de instancia
        this.usuarioActual = usuarioLogueado;

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
                case 1 ->
                    ejecutarListarUsuarios();
                case 2 ->
                    ejecutarRegistrarUsuario();
                case 3 ->
                    ejecutarEliminarUsuario();
            }
        } while (opcion != 0);
    }

    /**
     * Solicita al controlador el listado de usuarios y los imprime en consola.
     */
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

    /**
     * Captura los datos necesarios del usuario a registrar y solicita la
     * operación al controlador.
     */
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

    /**
     * Solicita el nombre del usuario a eliminar y confirma la acción con el
     * usuario activo.
     */
    private void ejecutarEliminarUsuario() {
        UIHelper.mostrarSubtitulo("ELIMINAR USUARIO");
        String nombre = UIHelper.leerTexto("Nombre de usuario a eliminar");

        if (UIHelper.leerBooleano("¿Confirma la eliminación?")) {
            try {
                // Paso el objeto usuarioActual 
                usuariosController.eliminarUsuario(nombre, this.usuarioActual);

                UIHelper.imprimirExito("Usuario eliminado correctamente.");
            } catch (Exception e) {
                UIHelper.imprimirError(e.getMessage());
            }
        }
        UIHelper.pausar();
    }
}
