package views;

import controllers.BancarioController;
import controllers.UsuariosController;
import java.util.List;
import models.Usuario;

// Menu Investigadores: Solo Lectura (Pueden Consultar todo)
public class MenuInvestigador {

    private final UsuariosController usuariosController;
    private final BancarioController bancarioController;

    public MenuInvestigador() {
        this.usuariosController = new UsuariosController();
        this.bancarioController = new BancarioController();
    }

    public void mostrarMenu(Usuario usuarioLogueado) {
        int opcion = -1;

        while (opcion != 0) {
            UIHelper.mostrarTitulo("Menú de Investigador - Hola, " + usuarioLogueado.getUsername());
            UIHelper.imprimirMensaje("--- ÁREA DE SISTEMAS ---\n"
                    + "1. Consultar todos los Usuarios\n\n"
                    + "--- ÁREA BANCARIA Y OPERATIVA ---\n"
                    + "2. Consultar listado de Vigilantes\n"
                    + "3. Consultar Sucursales y Entidades Bancarias\n"
                    + "4. Consultar Contratos de Vigilancia\n\n"
                    + "--- ÁREA JUDICIAL ---\n"
                    + "5. Consultar Personas Detenidas (Asaltantes)\n"
                    + "6. Consultar Bandas Organizadas\n"
                    + "7. Consultar Registro de Asaltos\n"
                    + "8. Consultar Registro de Jueces y Condenas\n\n"
                    + "0. Salir\n"
                    + "----------------------------------------");

            opcion = UIHelper.leerEntero("Seleccione una opción de consulta");

            switch (opcion) {
                case 1:
                    ejecutarListarUsuarios();
                    break;
                case 2:
                    ejecutarListarVigilantes();
                    break;
                case 3:
                    ejecutarListarSucursales();
                    break;
                case 4:
                    ejecutarListarContratos();
                    break;
                // Judicial
                case 5:
                case 6:
                case 7:
                case 8:
                    UIHelper.imprimirMensaje("\n[INFO] Módulo Judicial en desarrollo...");
                    break;
                case 0:
                    UIHelper.imprimirMensaje("\n[SISTEMA] Cerrando sesión de investigador. Volviendo al login...");
                    break;
                default:
                    UIHelper.imprimirError("Opción no válida. Intente otra vez.");
                    break;
            }
        }
    }

    // --- MÉTODOS DE CONSULTA  ---
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

   

    private void ejecutarListarVigilantes() {
        UIHelper.mostrarTitulo("LISTADO DE VIGILANTES");

        try {
            // Pido la lista al controlador bancario
            List<models.Vigilante> listaVigilantes = bancarioController.listarVigilantes();

            if (listaVigilantes.isEmpty()) {
                UIHelper.imprimirMensaje("\nNo hay vigilantes registrados en el sistema operativo.");
                return;
            }

            UIHelper.imprimirMensaje(
                    "----------------------------------------\n"
                    + "CÓDIGO VIGILANTE    | EDAD\n"
                    + "----------------------------------------"
            );

            for (models.Vigilante v : listaVigilantes) {
                System.out.printf("%-19s | %-4d años%n", v.getCodigo(), v.getEdad());
            }

            UIHelper.imprimirMensaje("----------------------------------------");

        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
    }

    private void ejecutarListarSucursales() {
        UIHelper.mostrarTitulo("LISTADO DE SUCURSALES BANCARIAS");

        try {
            List<models.Sucursal> listaSucursales = bancarioController.listarSucursales();

            if (listaSucursales.isEmpty()) {
                UIHelper.imprimirMensaje("\nNo hay sucursales registradas en el sistema.");
                return;
            }

            UIHelper.imprimirMensaje(
                    "--------------------------------------------------------------------------------\n"
                    + "SUCURSAL   | CÓD. BANCO | EMPLEADOS | DOMICILIO\n"
                    + "--------------------------------------------------------------------------------"
            );

            for (models.Sucursal sucursales : listaSucursales) {
                System.out.printf("%-10s | %-10s | %-9d | %-35s%n", 
                        sucursales.getCodigo(), 
                        sucursales.getEntidad().getCodigo(), 
                        sucursales.getNumeroEmpleados(), 
                        sucursales.getDomicilio());
            }

            UIHelper.imprimirMensaje("--------------------------------------------------------------------------------");

        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
    }

    private void ejecutarListarContratos() {
        UIHelper.mostrarTitulo("LISTADO DE CONTRATOS DE VIGILANCIA");

        try {
            List<models.ContratoVigilancia> listaContratos = bancarioController.listarContratosVigilancia();

            if (listaContratos.isEmpty()) {
                UIHelper.imprimirMensaje("\nNo hay contratos de vigilancia registrados.");
                return;
            }

            UIHelper.imprimirMensaje(
                    "------------------------------------------------------------------------\n"
                    + "SUCURSAL   | VIGILANTE  | FECHA CONTRATO | PORTA ARMA\n"
                    + "------------------------------------------------------------------------"
            );

            for (models.ContratoVigilancia c : listaContratos) {
                String llevaArma = c.isConArma() ? "SÍ" : "NO";
                
                System.out.printf("%-10s | %-10s | %-14s | %-10s%n", 
                        c.getSucursal().getCodigo(), 
                        c.getVigilante().getCodigo(), 
                        c.getFecha().toString(), 
                        llevaArma);
            }

            UIHelper.imprimirMensaje("------------------------------------------------------------------------");

        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
    }
}
