package views;

import controllers.AsaltosController;
import controllers.BancarioController;
import controllers.JudicialController;
import controllers.UsuariosController;
import java.util.List;
import models.Usuario;

// Menu Investigadores: Solo Lectura (Pueden Consultar todo)
public class MenuInvestigador {

    // Declaro los controladores necesarios para gestionar la lógica de cada área
    private final UsuariosController usuariosController;
    private final BancarioController bancarioController;
    private final JudicialController judicialController;
    private final AsaltosController asaltosController;

    public MenuInvestigador() {
        // Inicializo los controladores para acceder a la lógica de negocio
        this.usuariosController = new UsuariosController();
        this.bancarioController = new BancarioController();
        this.judicialController = new JudicialController();
        this.asaltosController = new AsaltosController();
    }

    public void mostrarMenu(Usuario usuarioLogueado) {
        int opcion;

        do {
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
                case 1 ->
                    ejecutarListarUsuarios();
                case 2 ->
                    ejecutarListarVigilantes();
                case 3 ->
                    ejecutarListarSucursales();
                case 4 ->
                    ejecutarListarContratos();
                case 5 ->
                    ejecutarListarDetenidos();
                case 6 ->
                    ejecutarListarBandas();
                case 7 ->
                    ejecutarListarAsaltos();
                case 8 ->
                    ejecutarListarJuecesYCondenas();
                case 0 ->
                    UIHelper.imprimirMensaje("\n[SISTEMA] Cerrando sesión de investigador. Volviendo al login...");
                default ->
                    UIHelper.imprimirError("Opción no válida. Intente otra vez.");
            }

        } while (opcion != 0);

    }

    // --- MÉTODOS DE CONSULTA  ---
    //Opcion Mostrar Usuarios Registrados
    private void ejecutarListarUsuarios() {
        UIHelper.mostrarSubtitulo("LISTADO DE USUARIOS");

        try {
            // Pido la lista de usuarios al controlador
            List<Usuario> listaUsuarios = usuariosController.listarUsuarios();

            // Verifico si el archivo vino vacío 
            if (listaUsuarios.isEmpty()) {
                UIHelper.imprimirMensaje("\n No hay usuarios registrados en el sistema actualmente.");
            } else {

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
            }

        } catch (Exception e) {
            // Excepción si el DAO falla al leer el .txt
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar();
    }

    //Opcion Mostrar Vigilantes
    private void ejecutarListarVigilantes() {
        UIHelper.mostrarSubtitulo("LISTADO DE VIGILANTES");

        try {
            // Pido la lista al controlador bancario
            List<models.Vigilante> listaVigilantes = bancarioController.listarVigilantes();

            if (listaVigilantes.isEmpty()) {
                UIHelper.imprimirMensaje("\nNo hay vigilantes registrados en el sistema operativo.");
            } else {
                UIHelper.imprimirMensaje(
                        "----------------------------------------\n"
                        + "CÓDIGO VIGILANTE    | EDAD\n"
                        + "----------------------------------------"
                );

                for (models.Vigilante v : listaVigilantes) {
                    System.out.printf("%-19s | %-4d años%n", v.getCodigo(), v.getEdad());
                }

                UIHelper.imprimirMensaje("----------------------------------------");
            }

        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar();
    }

    //Opcion Mostrar Sucursales
    private void ejecutarListarSucursales() {
        UIHelper.mostrarSubtitulo("LISTADO DE SUCURSALES BANCARIAS");

        try {
            List<models.Sucursal> listaSucursales = bancarioController.listarSucursales();

            if (listaSucursales.isEmpty()) {
                UIHelper.imprimirMensaje("\nNo hay sucursales registradas en el sistema.");
            } else {

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
            }

        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar();
    }

    //Opcion Mostrar Contratos
    private void ejecutarListarContratos() {
        UIHelper.mostrarSubtitulo("LISTADO DE CONTRATOS DE VIGILANCIA");

        try {
            List<models.ContratoVigilancia> listaContratos = bancarioController.listarContratosVigilancia();

            if (listaContratos.isEmpty()) {
                UIHelper.imprimirMensaje("\nNo hay contratos de vigilancia registrados.");
            } else {

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
            }
            UIHelper.imprimirMensaje("------------------------------------------------------------------------");

        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar();
    }

    //Opcion Mostrar Detenidos
    private void ejecutarListarDetenidos() {
        UIHelper.mostrarSubtitulo("REGISTRO DE DETENIDOS");
        try {
            // Listamos los CasoJudicial con condena
            List<models.CasoJudicial> lista = judicialController.listarDetenidos();
            //Si no hay casos, avisamos al usuario
            if (lista.isEmpty()) {
                UIHelper.imprimirMensaje("\nNo hay  detenidos registrados en el sistema.");
            } else {

                UIHelper.imprimirMensaje("--------------------------------------------------------------------------\n"
                        + "ASALTANTE          | ID ASALTO | FECHA      | MESES\n"
                        + "--------------------------------------------------------------------------");

                for (models.CasoJudicial c : lista) {
                    System.out.printf("%-18s | %-9s | %-10s | %-5d%n",
                            c.getAsalto().getAsaltante().getNombreCompleto(),
                            c.getAsalto().getIdAsalto(),
                            c.getAsalto().getFecha().toString(),
                            c.getMesesCarcel());
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError("Error al cargar datos: " + e.getMessage());
        }
        UIHelper.pausar();
    }

    //Opcion Mostrar Bandas 
    private void ejecutarListarBandas() {
        UIHelper.mostrarSubtitulo("LISTADO DE BANDAS ORGANIZADAS");
        try {
            //Llamo a asaltosController
            List<models.Banda> listaBandas = asaltosController.listarBandas();

            if (listaBandas.isEmpty()) {
                UIHelper.imprimirMensaje("\nNo hay bandas organizadas registradas.");
            } else {
                UIHelper.imprimirMensaje("----------------------------------------\n"
                        + "NÚMERO BANDA | MIEMBROS\n"
                        + "----------------------------------------");
                for (models.Banda b : listaBandas) {
                    System.out.printf("%-12s | %-8d%n", b.getNumeroBanda(), b.getCantMiembros());
                }
                UIHelper.imprimirMensaje("----------------------------------------");
            }
        } catch (Exception e) {
            UIHelper.imprimirError("Error al cargar bandas: " + e.getMessage());
        }
        UIHelper.pausar();
    }

    // Opcion Mostrar Registro de Asaltos
    private void ejecutarListarAsaltos() {
        UIHelper.mostrarSubtitulo("REGISTRO DE ASALTOS");

        try {
            // Solicito la lista al controlador
            List<models.Asalto> listaAsaltos = asaltosController.listarAsaltos();

            if (listaAsaltos.isEmpty()) {
                UIHelper.imprimirMensaje("\nNo hay registros de asaltos en el sistema.");
            } else {
                UIHelper.imprimirMensaje(
                        "--------------------------------------------------------------------------\n"
                        + "ID ASALTO | ASALTANTE        | SUCURSAL   | FECHA\n"
                        + "--------------------------------------------------------------------------"
                );

                for (models.Asalto a : listaAsaltos) {
                    System.out.printf("%-9s | %-16s | %-10s | %-10s%n",
                            a.getIdAsalto(),
                            a.getAsaltante().getNombreCompleto(),
                            a.getSucursal().getCodigo(),
                            a.getFecha().toString());
                }

                UIHelper.imprimirMensaje("--------------------------------------------------------------------------");
            }
        } catch (Exception e) {
            UIHelper.imprimirError("Error al cargar asaltos: " + e.getMessage());
        }
        UIHelper.pausar();
    }

    //Opcion Mostrar Jueces y Condenas
    private void ejecutarListarJuecesYCondenas() {
        UIHelper.mostrarSubtitulo("REGISTRO DE JUECES Y CONDENAS");
        try {
            // Listamos los CasoJudicial
            List<models.CasoJudicial> lista = judicialController.listarCasosJudiciales();
            //Si no hay casos, avisamos al usuario
            if (lista.isEmpty()) {
                UIHelper.imprimirMensaje("\nNo hay expedientes judiciales registrados.");
            } else {
                UIHelper.imprimirMensaje("------------------------------------------------------------------\n"
                        + "JUEZ              | ASALTANTE          | CONDENA | MESES\n"
                        + "------------------------------------------------------------------");

                for (models.CasoJudicial c : lista) {
                    System.out.printf("%-17s | %-18s | %-7s | %-5d%n",
                            c.getJuez().getNombre(),
                            c.getAsalto().getAsaltante().getNombreCompleto(),
                            c.isCondenado() ? "SÍ" : "NO",
                            c.getMesesCarcel());
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError("Error al cargar datos: " + e.getMessage());
        }
        UIHelper.pausar();
    }
}
