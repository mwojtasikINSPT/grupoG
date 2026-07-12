package views.submenues;

import controllers.BancarioController;
import java.util.List;
import models.ContratoVigilancia;
import models.Rol;
import models.Sucursal;
import models.Vigilante;
import views.UIHelper;

/**
 * Clase responsable de gestionar las operaciones principales del sistema.
 * Permite listar y registrar vigilantes, sucursales y contratos de vigilancia,
 * mostrando un menú interactivo en consola.
 *
 * Las opciones disponibles dependen del rol del usuario: - Los usuarios con rol
 * ADMINISTRADOR pueden acceder a las operaciones de escritura (registro de
 * vigilantes, sucursales y contratos). - Los demás roles solo pueden consultar
 * listados.
 *
 * Esta clase actúa como puente entre la capa de presentación (UIHelper) y los
 * controladores de negocio BancarioController.
 *
 * @author GrupoG
 */
public class GestionOperativa {

    private final BancarioController bancarioController;

    /**
     * Constructor por defecto. Inicializa los controladores de usuarios y
     * bancario para gestionar las operaciones disponibles.
     */
    public GestionOperativa() {
        this.bancarioController = new BancarioController();
    }

    /**
     * Muestra el menú de gestión operativa en consola. Permite listar y
     * registrar entidades según el rol del usuario.
     *
     * @param rolUsuario Rol del usuario que accede al menú. Determina las
     * opciones disponibles.
     */
    public void mostrar(Rol rolUsuario) {
        int opcion;
        do {
            UIHelper.mostrarSubtitulo("GESTIÓN OPERATIVA");
            UIHelper.imprimirMensaje("1. Listar Vigilantes\n2. Listar Sucursales\n3. Listar Contratos");

            // Solo Administrador ve las opciones de escritura
            if (rolUsuario == Rol.ADMINISTRADOR) {
                UIHelper.imprimirMensaje("4. Registrar Vigilante\n5. Registrar Sucursal\n6. Registrar Contrato");
            }
            UIHelper.imprimirMensaje("0. Volver");

            opcion = UIHelper.leerEntero("Seleccione opción");

            // Protección: Si no es admin y elige una opción prohibida, bloqueamos
            if (opcion >= 4 && opcion <= 6 && rolUsuario != Rol.ADMINISTRADOR) {
                UIHelper.imprimirError("Acceso denegado: Operación solo para Administradores.");
                continue;
            }

            switch (opcion) {
                case 1 ->
                    ejecutarListarVigilantes();
                case 2 ->
                    ejecutarListarSucursales();
                case 3 ->
                    ejecutarListarContratos();
                case 4 ->
                    ejecutarRegistrarVigilante();
                case 5 ->
                    ejecutarRegistrarSucursal();
                case 6 ->
                    ejecutarRegistrarContrato();
            }
        } while (opcion != 0);
    }

    /**
     * Lista todos los vigilantes registrados en el sistema. Muestra código y
     * edad de cada vigilante. Captura y muestra errores en caso de fallos.
     */
    private void ejecutarListarVigilantes() {
        UIHelper.mostrarSubtitulo("LISTADO DE VIGILANTES");
        try {
            List<Vigilante> lista = bancarioController.listarVigilantes();
            if (lista.isEmpty()) {
                UIHelper.imprimirMensaje("No hay vigilantes registrados.");
            } else {
                UIHelper.imprimirMensaje("CÓDIGO      | EDAD");
                UIHelper.imprimirMensaje("-----------------------");
                for (Vigilante v : lista) {
                    // Imprimo los datos
                    System.out.printf("%-11s | %-5d%n", v.getCodigo(), v.getEdad());
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar();
    }

    /**
     * Lista todas las sucursales bancarias registradas. Muestra código,
     * domicilio y número de empleados. Captura y muestra errores en caso de
     * fallos.
     */
    private void ejecutarListarSucursales() {
        UIHelper.mostrarSubtitulo("LISTADO DE SUCURSALES");
        try {
            List<Sucursal> lista = bancarioController.listarSucursales();
            if (lista.isEmpty()) {
                UIHelper.imprimirMensaje("No hay sucursales registradas.");
            } else {
                UIHelper.imprimirMensaje("CÓDIGO      | DOMICILIO        | EMPLEADOS");
                UIHelper.imprimirMensaje("------------------------------------------");
                for (Sucursal s : lista) {
                    System.out.printf("%-11s | %-16s | %-9d%n",
                            s.getCodigo(),
                            s.getDomicilio(),
                            s.getNumeroEmpleados());
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar();
    }

    /**
     * Lista todos los contratos de vigilancia registrados. Muestra sucursal,
     * vigilante, fecha y si porta arma. Captura y muestra errores en caso de
     * fallos.
     */
    private void ejecutarListarContratos() {
        UIHelper.mostrarSubtitulo("LISTADO DE CONTRATOS");
        try {
            List<ContratoVigilancia> lista = bancarioController.listarContratosVigilancia();
            if (lista.isEmpty()) {
                UIHelper.imprimirMensaje("No hay contratos registrados.");
            } else {
                UIHelper.imprimirMensaje("SUCURSAL    | VIGILANTE   | FECHA      | ARMA");
                UIHelper.imprimirMensaje("----------------------------------------------");
                for (ContratoVigilancia c : lista) {
                    System.out.printf("%-11s | %-11s | %-10s | %s%n",
                            c.getSucursal().getCodigo(),
                            c.getVigilante().getCodigo(),
                            c.getFecha(),
                            (c.isConArma() ? "Sí" : "No"));
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar();
    }

    /**
     * Registra un nuevo vigilante en el sistema. Solicita código y edad,
     * validando que sea mayor de edad. Captura y muestra errores en caso de
     * datos inválidos.
     */
    private void ejecutarRegistrarVigilante() {
        UIHelper.mostrarSubtitulo("REGISTRO DE EMPLEADO: VIGILANTE");

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
        UIHelper.pausar();
    }

    /**
     * Registra una nueva sucursal bancaria en el sistema. Solicita código,
     * domicilio, número de empleados y banco asociado. Captura y muestra
     * errores en caso de datos inválidos.
     */
    private void ejecutarRegistrarSucursal() {
        UIHelper.mostrarSubtitulo("REGISTRO DE SUCURSAL BANCARIA");

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
        UIHelper.pausar();
    }

    /**
     * Registra un nuevo contrato de vigilancia. Solicita sucursal, código de
     * contrato, vigilante, fecha y si porta arma. Captura y muestra errores en
     * caso de datos inválidos.
     */
    private void ejecutarRegistrarContrato() {
        UIHelper.mostrarSubtitulo("REGISTRO DE CONTRATO DE VIGILANCIA");

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
        UIHelper.pausar();
    }

}
