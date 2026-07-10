package views;

// Consola específica con acceso total (CRUD completo).
import controllers.BancarioController;
import controllers.UsuariosController;
import models.Usuario;

public class MenuAdministrador {

    private final UsuariosController usuariosController;
    private final BancarioController bancarioControler;

    public MenuAdministrador() {
        this.usuariosController = new UsuariosController();
        this.bancarioControler = new BancarioController();
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
    public void ejecutarRegistrarUsuario() {
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
