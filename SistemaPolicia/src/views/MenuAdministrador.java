package views;

import controllers.BancarioController;
import controllers.UsuariosController;
import java.util.List;
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
            UIHelper.mostrarTitulo("MENÚ DE ADMIN - Hola, " + usuarioLogueado.getUsername());
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
                    ejecutarEliminarUsuario(usuarioLogueado);
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
            UIHelper.imprimirExito("El usuario " + nombreUsuario + " fue registrado correctamente.");
        } catch (Exception e) {
            // Si usuario repetido, vacíos, etc.
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar();
    }

    private void ejecutarListarUsuarios() {
        UIHelper.mostrarTitulo("LISTADO DE USUARIOS");

        try {
            // Pido la lista de usuarios al controlador
            List<Usuario> listaUsuarios = usuariosController.listarUsuarios();

            // Verifico si el archivo vino vacío 
            if (listaUsuarios.isEmpty()) {
                UIHelper.imprimirMensaje("\n No hay usuarios registrados en el sistema actualmente.");
                return;
            }

            // Cabecera 
            UIHelper.imprimirMensaje(
                    "--------------------------------------------------\n"
                    + "NOMBRE DE USUARIO         | ROL\n"
                    + "--------------------------------------------------"
            );

            // Recorro la lista e imprimo cada usuario respetando el espaciado
            for (Usuario u : listaUsuarios) {
                System.out.printf("%-25s | %-20s%n", u.getUsername(), u.obtenerRol().name());
            }

            UIHelper.imprimirMensaje("--------------------------------------------------");

        } catch (Exception e) {
            // Excepción si el DAO falla al leer el .txt
            UIHelper.imprimirError(e.getMessage());
        }
    }

    private void ejecutarEliminarUsuario(Usuario usuarioLogueado) {
        UIHelper.mostrarTitulo("ELIMINAR USUARIO");

        // Pido el nombre del usuario a borrar
        String usuarioAEliminar = UIHelper.leerTexto("Ingrese el nombre de usuario que desea eliminar");

        // Pido la doble confirmación usando la herramienta que ya tenés
        boolean confirmar = UIHelper.leerBooleano("¿Está seguro que desea eliminar al usuario '" + usuarioAEliminar + "'? Esta acción no se puede deshacer.");

        // 'N' cancela operación
        if (!confirmar) {
            UIHelper.imprimirMensaje("\nOperación cancelada. El usuario no fue eliminado.");
            return;
        }

        try {
            // 'S' paso al controlador
            usuariosController.eliminarUsuario(usuarioAEliminar, usuarioLogueado.getUsername());
            UIHelper.imprimirExito("\nEl usuario '" + usuarioAEliminar + "' fue eliminado correctamente.");

        } catch (Exception e) {
            // Error si no existe o si intenta borrarse a sí mismo
            UIHelper.imprimirError(e.getMessage());
        }
    }

    private void ejecutarRegistrarVigilante() {
        UIHelper.mostrarTitulo("REGISTRO DE EMPLEADO: VIGILANTE");

        // Pido los datos
        String codigo = UIHelper.leerTexto("Ingrese el código identificador del vigilante");
        int edad = UIHelper.leerEntero("Ingrese la edad del vigilante (mínimo 18)");

        try {
            // Llamamos al controlador
            bancarioController.registrarVigilante(codigo, edad);
            UIHelper.imprimirExito("\nEl vigilante con código '" + codigo + "' fue registrado en la base de datos operativa.");

        } catch (Exception e) {
            // Atajamos edad < 18, código duplicado, etc.
            UIHelper.imprimirError(e.getMessage());
        }
    }

   private void ejecutarRegistrarSucursal() {
        UIHelper.mostrarTitulo("REGISTRO DE SUCURSAL BANCARIA");

        // Pedimos los datos 
        String codigoSucursal = UIHelper.leerTexto("Ingrese el código de la nueva sucursal");
        String domicilio = UIHelper.leerTexto("Ingrese el domicilio de la sucursal");
        int numEmpleados = UIHelper.leerEntero("Ingrese la cantidad de empleados de la sucursal");
        String codigoBanco = UIHelper.leerTexto("Ingrese el código de la Entidad Bancaria a la que pertenece");

        try {
            // Mandamos los datos al controlador
            bancarioController.registrarSucursal(codigoSucursal, domicilio, numEmpleados, codigoBanco);
            UIHelper.imprimirExito("\nLa sucursal '" + codigoSucursal + "' fue registrada y vinculada al banco '" + codigoBanco + "' correctamente.");
            
        } catch (Exception e) {
            // Atajamos sucursal duplicada, banco inexistente, vacíos, etc.
            UIHelper.imprimirError(e.getMessage());
        }
    }

  private void ejecutarRegistrarContrato() {
        UIHelper.mostrarTitulo("REGISTRO DE CONTRATO DE VIGILANCIA");

        // Pedimos los datos 
        String codigoSucursal = UIHelper.leerTexto("Ingrese el código de la sucursal bancaria");
        String codigoVigilancia = UIHelper.leerTexto("Ingrese un código identificador para este contrato");
        String codigoVigilante = UIHelper.leerTexto("Ingrese el código del vigilante a contratar");
        String fechaStr = UIHelper.leerTexto("Ingrese la fecha del contrato (Formato obligatorio: AAAA-MM-DD)");
        boolean conArma = UIHelper.leerBooleano("¿El vigilante portará arma durante este contrato?");

        try {
            // Mandamos los datos al controlador
            bancarioController.registrarContratoVigilancia(codigoSucursal, codigoVigilancia, codigoVigilante, fechaStr, conArma);
            UIHelper.imprimirExito("\nEl contrato '" + codigoVigilancia + "' fue registrado correctamente.");
            
        } catch (Exception e) {
            // Atajamos sucursal inexistente, vigilante inexistente, mal formato de fecha, etc.
            UIHelper.imprimirError(e.getMessage());
        }
    }

}
