package views;

import controllers.BancarioController;
import controllers.UsuariosController;
import models.Rol;
import models.Usuario;

// Consola específica con acceso total (CRUD completo).
public class MenuAdministrador {

    private final UsuariosController usuariosController;
    private final BancarioController bancarioController;

    public MenuAdministrador() {
        this.usuariosController = new UsuariosController();
        this.bancarioController = new BancarioController();
    }

    //Mostrar menu del administrador
    public void mostrarMenu(Usuario usuarioLogueado) {
        int opcion = -1;

        while (opcion != 0) {
            UIHelper.mostrarTitulo("Menú de Administrador - Hola, " + usuarioLogueado.getUsername());
            UIHelper.imprimirMensaje("--- GESTIÓN DE USUARIOS ---\n"
                    + "1. Registrar un nuevo Usuario en el sistema\n"
                    + "2. Listar todos los Usuarios\n"
                    + "3. Eliminar un Usuario\n\n"
                    + "--- GESTIÓN OPERATIVA ---\n"
                    + "4. Registrar un nuevo Vigilante de Seguridad\n"
                    + "5. Registrar una nueva Sucursal Bancaria\n"
                    + "6. Registrar un Contrato de Vigilancia\n\n"
                    + "0. Cerrar Sesión / Salir\n"
                    + "----------------------------------------");

            opcion = UIHelper.leerEntero("Seleccione una opción");

            switch (opcion) {
                case 1:
                    ejecutarRegistrarUsuario();
                    break;
                case 2:
                    ejecutarListarUsuarios();
                    break;
                case 3:
                    ejecutarEliminarUsuario();
                    break;
                case 4:
                    ejecutarRegistrarVigilante();
                    break;
                case 5:
                    ejecutarRegistrarSucursal();
                    break;
                case 6:
                    ejecutarRegistrarContrato();
                    break;
                case 0:
                    UIHelper.imprimirMensaje("\n[SISTEMA] Cerrando sesión de administrador. Volviendo al login...");
                    break;
                default:
                    UIHelper.imprimirError("Opción no válida. Intente otra vez.");
                    break;
            }
        }
    }

    //METODOS QUE LLAMAN A LOS CONTROLLERS
    //falta armar
    private void ejecutarRegistrarUsuario() {
        UIHelper.mostrarTitulo("REGISTRO DE NUEVO USUARIO");

        // Pido los datos 
        String nombreUsuario = UIHelper.leerTexto("Ingrese el nombre de usuario");
        String password = UIHelper.leerTexto("Ingrese la contraseña");
        Rol rol = UIHelper.leerRol("Ingrese el rol (ADMINISTRADOR, INVESTIGADOR, VIGILANTE)");

        // Verifico si es vigilante para pedirle su código 
        String codigoVigilante = null;
        if (rol == Rol.VIGILANTE) {
            codigoVigilante = UIHelper.leerTexto("Ingrese el código único del vigilante");
        }

        // Mando al controlador 
        try {
            usuariosController.registrarUsuario(nombreUsuario, password, rol, codigoVigilante);
            UIHelper.imprimirExito("El usuario '\" + nombreUsuario + \"' fue registrado correctamente.\"");
        } catch (Exception e) {
            // Si el controlador se queja (usuario repetido, vacíos, etc.), lo atajamos acá
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar(); 
    }

    public void ejecutarListarUsuarios() {
            }

    public void ejecutarEliminarUsuario() {
    }

    public void ejecutarRegistrarVigilante() {
    }

    public void ejecutarRegistrarSucursal() {
    }

    public void ejecutarRegistrarContrato() {
    }

}
