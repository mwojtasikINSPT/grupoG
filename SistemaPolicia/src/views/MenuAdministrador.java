package views;

// Consola específica con acceso total (CRUD completo).

import controllers.BancarioController;
import controllers.UsuariosController;
import models.Usuario;

public class MenuAdministrador {

    private final UsuariosController usuariosController;
    private final BancarioController bancarioControler;
    
    public MenuAdministrador(){
        this.usuariosController = new UsuariosController();
        this.bancarioControler = new BancarioController();
    }
    
    //Mostrar menu del administrador
    public void mostrarMenu(Usuario usuarioLogueado) {
        int opcion = -1;

        while (opcion != 0) {
            UIHelper.mostrarTitulo("Menú de Administrador - Hola, " + usuarioLogueado.getUsername());
            System.out.println("1. Registrar un nuevo Usuario en el sistema");
            System.out.println("2. Listar todos los Usuarios");
            System.out.println("3. Eliminar un Usuario");
            System.out.println("4. Registrar un nuevo Vigilante de Seguridad");
            System.out.println("5. Registrar una nueva Sucursal Bancaria");
            System.out.println("6. Registrar un Contrato de Vigilancia");
            System.out.println("0. Cerrar Sesión / Salir");
            System.out.println("----------------------------------------");
            
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
                    System.out.println("\n[SISTEMA] Cerrando sesión de administrador. Volviendo al login...");
                    break;
                default:
                    System.out.println("[ERROR] Opción no válida. Intente otra vez.");
                    break;
            }
        }
    }
    
    //METODOS QUE LLAMAN A LOS CONTROLLERS
    
    //falta armar
    
    public void ejecutarRegistrarUsuario(){}
    
    public void ejecutarListarUsuarios(){}
    
    public void ejecutarEliminarUsuario(){}
    
    public void ejecutarRegistrarVigilante(){}
    
    public void ejecutarRegistrarSucursal(){}
    
    public void ejecutarRegistrarContrato(){}
    
    
}
