package views.submenues;

import controllers.BancarioController;
import java.util.List;
import models.ContratoVigilancia;
import models.EntidadBancaria;
import models.Rol;
import models.Sucursal;
import models.Vigilante;
import views.UIHelper;

/**
 * Presenta las consultas y operaciones del módulo bancario.
 * Las opciones disponibles dependen del rol del usuario.
 *
 * @author GrupoG
 */
public class GestionOperativa {

    private final BancarioController bancarioController;

    /**
     * Crea la vista con el controlador utilizado por la aplicación.
     */
    public GestionOperativa() {
        this(new BancarioController());
    }

    /**
     * Crea la vista con el controlador recibido.
     *
     * @param bancarioController controlador de operaciones bancarias
     * @throws IllegalArgumentException si el controlador es nulo
     */
    public GestionOperativa(
            BancarioController bancarioController) {

        if (bancarioController == null) {
            throw new IllegalArgumentException(
                    "El controlador bancario es obligatorio."
            );
        }

        this.bancarioController = bancarioController;
    }

    /**
     * Muestra las opciones permitidas para el rol recibido.
     *
     * @param rolUsuario rol del usuario que accede al menú
     */
    public void mostrar(Rol rolUsuario) {
        int opcion;

        do {
            UIHelper.mostrarSubtitulo("GESTIÓN OPERATIVA");

            UIHelper.imprimirMensaje(
                    "1. Listar Vigilantes\n"
                    + "2. Listar Sucursales\n"
                    + "3. Listar Contratos\n"
                    + "4. Listar Entidades Bancarias"
            );

            if (rolUsuario == Rol.ADMINISTRADOR) {
                UIHelper.imprimirMensaje(
                        "5. Registrar Vigilante\n"
                        + "6. Registrar Sucursal\n"
                        + "7. Registrar Contrato\n"
                        + "8. Registrar Entidad Bancaria\n"
                        + "9. Eliminar Vigilante\n"
                        + "10. Eliminar Sucursal\n"
                        + "11. Eliminar Contrato\n"
                        + "12. Eliminar Entidad Bancaria"
                );
            }

            UIHelper.imprimirMensaje("0. Volver");

            opcion = UIHelper.leerEntero("Seleccione opción");

            if (opcion >= 5
                    && opcion <= 12
                    && rolUsuario != Rol.ADMINISTRADOR) {

                UIHelper.imprimirError(
                        "Acceso denegado: operación exclusiva "
                        + "para administradores."
                );
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
                    ejecutarListarEntidadesBancarias();

                case 5 ->
                    ejecutarRegistrarVigilante();

                case 6 ->
                    ejecutarRegistrarSucursal();

                case 7 ->
                    ejecutarRegistrarContrato();

                case 8 ->
                    ejecutarRegistrarEntidadBancaria();

                case 9 ->
                    ejecutarEliminarVigilante();

                case 10 ->
                    ejecutarEliminarSucursal();

                case 11 ->
                    ejecutarEliminarContrato();

                case 12 ->
                    ejecutarEliminarEntidadBancaria();

                case 0 ->
                    UIHelper.imprimirMensaje(
                            "Volviendo al menú principal..."
                    );

                default ->
                    UIHelper.imprimirError(
                            "Opción inválida. Intente nuevamente."
                    );
            }

        } while (opcion != 0);
    }

    /**
     * Muestra los vigilantes registrados.
     */
    private void ejecutarListarVigilantes() {
        UIHelper.mostrarSubtitulo("LISTADO DE VIGILANTES");

        try {
            List<Vigilante> lista =
                    bancarioController.listarVigilantes();

            if (lista.isEmpty()) {
                UIHelper.imprimirMensaje(
                        "No hay vigilantes registrados."
                );
            } else {
                UIHelper.imprimirMensaje("CÓDIGO      | EDAD");
                UIHelper.imprimirMensaje("-----------------------");

                for (Vigilante vigilante : lista) {
                    System.out.printf(
                            "%-11s | %-5d%n",
                            vigilante.getCodigo(),
                            vigilante.getEdad()
                    );
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }

        UIHelper.pausar();
    }

    /**
     * Muestra las sucursales registradas.
     */
    private void ejecutarListarSucursales() {
        UIHelper.mostrarSubtitulo("LISTADO DE SUCURSALES");

        try {
            List<Sucursal> lista =
                    bancarioController.listarSucursales();

            if (lista.isEmpty()) {
                UIHelper.imprimirMensaje(
                        "No hay sucursales registradas."
                );
            } else {
                UIHelper.imprimirMensaje(
                        "CÓDIGO      | DOMICILIO        | EMPLEADOS"
                );
                UIHelper.imprimirMensaje(
                        "------------------------------------------"
                );

                for (Sucursal sucursal : lista) {
                    System.out.printf(
                            "%-11s | %-16s | %-9d%n",
                            sucursal.getCodigo(),
                            sucursal.getDomicilio(),
                            sucursal.getNumeroEmpleados()
                    );
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }

        UIHelper.pausar();
    }

    /**
     * Muestra los contratos de vigilancia registrados.
     */
    private void ejecutarListarContratos() {
        UIHelper.mostrarSubtitulo("LISTADO DE CONTRATOS");

        try {
            List<ContratoVigilancia> lista =
                    bancarioController.listarContratosVigilancia();

            if (lista.isEmpty()) {
                UIHelper.imprimirMensaje(
                        "No hay contratos registrados."
                );
            } else {
                UIHelper.imprimirMensaje(
                        "SUCURSAL    | VIGILANTE   | FECHA      | ARMA"
                );
                UIHelper.imprimirMensaje(
                        "----------------------------------------------"
                );

                for (ContratoVigilancia contrato : lista) {
                    System.out.printf(
                            "%-11s | %-11s | %-10s | %s%n",
                            contrato.getIdSucursal(),
                            contrato.getIdVigilante(),
                            contrato.getFecha(),
                            contrato.isConArma() ? "Sí" : "No"
                    );
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }

        UIHelper.pausar();
    }

    /**
     * Muestra las entidades bancarias registradas.
     */
    private void ejecutarListarEntidadesBancarias() {
        UIHelper.mostrarSubtitulo(
                "LISTADO DE ENTIDADES BANCARIAS"
        );

        try {
            List<EntidadBancaria> lista =
                    bancarioController.listarEntidadesBancarias();

            if (lista.isEmpty()) {
                UIHelper.imprimirMensaje(
                        "No hay entidades bancarias registradas."
                );
            } else {
                UIHelper.imprimirMensaje(
                        "CÓDIGO      | DOMICILIO CENTRAL"
                );
                UIHelper.imprimirMensaje(
                        "------------------------------------------"
                );

                for (EntidadBancaria entidad : lista) {
                    System.out.printf(
                            "%-11s | %-25s%n",
                            entidad.getCodigo(),
                            entidad.getDomicilioCentral()
                    );
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }

        UIHelper.pausar();
    }

    /**
     * Solicita los datos y registra un vigilante.
     */
    private void ejecutarRegistrarVigilante() {
        UIHelper.mostrarSubtitulo(
                "REGISTRO DE EMPLEADO: VIGILANTE"
        );

        String codigo = UIHelper.leerTexto(
                "Ingrese el código identificador del vigilante"
        );

        int edad = UIHelper.leerEntero(
                "Ingrese la edad del vigilante (mínimo 18)"
        );

        try {
            bancarioController.registrarVigilante(codigo, edad);

            UIHelper.imprimirExito(
                    "\nEl vigilante con código '" + codigo
                    + "' fue registrado correctamente."
            );
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }

        UIHelper.pausar();
    }

    /**
     * Solicita los datos y registra una sucursal.
     */
    private void ejecutarRegistrarSucursal() {
        UIHelper.mostrarSubtitulo(
                "REGISTRO DE SUCURSAL BANCARIA"
        );

        String codigoSucursal = UIHelper.leerTexto(
                "Ingrese el código de la nueva sucursal"
        );

        String domicilio = UIHelper.leerTexto(
                "Ingrese el domicilio de la sucursal"
        );

        int numEmpleados = UIHelper.leerEntero(
                "Ingrese la cantidad de empleados de la sucursal"
        );

        String codigoBanco = UIHelper.leerTexto(
                "Ingrese el código de la entidad bancaria "
                + "a la que pertenece"
        );

        try {
            bancarioController.registrarSucursal(
                    codigoSucursal,
                    domicilio,
                    numEmpleados,
                    codigoBanco
            );

            UIHelper.imprimirExito(
                    "\nLa sucursal '" + codigoSucursal
                    + "' fue registrada correctamente."
            );
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }

        UIHelper.pausar();
    }

    /**
     * Solicita los datos y registra un contrato.
     */
    private void ejecutarRegistrarContrato() {
        UIHelper.mostrarSubtitulo(
                "REGISTRO DE CONTRATO DE VIGILANCIA"
        );

        String codigoSucursal = UIHelper.leerTexto(
                "Ingrese el código de la sucursal bancaria"
        );

        String codigoVigilante = UIHelper.leerTexto(
                "Ingrese el código del vigilante a contratar"
        );

        String fechaStr = UIHelper.leerTexto(
                "Ingrese la fecha del contrato "
                + "(Formato obligatorio: AAAA-MM-DD)"
        );

        boolean conArma = UIHelper.leerBooleano(
                "¿El vigilante portará arma durante este contrato?"
        );

        try {
            bancarioController.registrarContratoVigilancia(
                    codigoSucursal,
                    codigoVigilante,
                    fechaStr,
                    conArma
            );

            UIHelper.imprimirExito(
                    "\nEl contrato fue registrado correctamente."
            );
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }

        UIHelper.pausar();
    }

    /**
     * Solicita los datos y registra una entidad bancaria.
     */
    private void ejecutarRegistrarEntidadBancaria() {
        UIHelper.mostrarSubtitulo(
                "REGISTRO DE ENTIDAD BANCARIA"
        );

        String codigo = UIHelper.leerTexto(
                "Ingrese el código de la entidad bancaria"
        );

        String domicilioCentral = UIHelper.leerTexto(
                "Ingrese el domicilio de la central"
        );

        try {
            bancarioController.registrarEntidadBancaria(
                    codigo,
                    domicilioCentral
            );

            UIHelper.imprimirExito(
                    "La entidad bancaria fue registrada correctamente."
            );
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }

        UIHelper.pausar();
    }

    /**
     * Solicita un código y elimina un vigilante.
     */
    private void ejecutarEliminarVigilante() {
        UIHelper.mostrarSubtitulo("ELIMINAR VIGILANTE");

        String codigo = UIHelper.leerTexto(
                "Ingrese el código del vigilante a eliminar"
        );

        if (UIHelper.leerBooleano(
                "¿Confirma la eliminación del vigilante?"
        )) {
            try {
                bancarioController.eliminarVigilante(codigo);

                UIHelper.imprimirExito(
                        "El vigilante fue eliminado correctamente."
                );
            } catch (Exception e) {
                UIHelper.imprimirError(e.getMessage());
            }
        } else {
            UIHelper.imprimirMensaje("Eliminación cancelada.");
        }

        UIHelper.pausar();
    }

    /**
     * Solicita un código y elimina una sucursal.
     */
    private void ejecutarEliminarSucursal() {
        UIHelper.mostrarSubtitulo("ELIMINAR SUCURSAL");

        String codigo = UIHelper.leerTexto(
                "Ingrese el código de la sucursal a eliminar"
        );

        if (UIHelper.leerBooleano(
                "¿Confirma la eliminación de la sucursal?"
        )) {
            try {
                bancarioController.eliminarSucursal(codigo);

                UIHelper.imprimirExito(
                        "La sucursal fue eliminada correctamente."
                );
            } catch (Exception e) {
                UIHelper.imprimirError(e.getMessage());
            }
        } else {
            UIHelper.imprimirMensaje("Eliminación cancelada.");
        }

        UIHelper.pausar();
    }

    /**
     * Solicita los datos y elimina un contrato.
     */
    private void ejecutarEliminarContrato() {
        UIHelper.mostrarSubtitulo(
                "ELIMINAR CONTRATO DE VIGILANCIA"
        );

        String codigoSucursal = UIHelper.leerTexto(
                "Ingrese el código de la sucursal"
        );

        String codigoVigilante = UIHelper.leerTexto(
                "Ingrese el código del vigilante"
        );

        String fechaStr = UIHelper.leerTexto(
                "Ingrese la fecha del contrato "
                + "(Formato obligatorio: AAAA-MM-DD)"
        );

        if (UIHelper.leerBooleano(
                "¿Confirma la eliminación del contrato?"
        )) {
            try {
                bancarioController.eliminarContratoVigilancia(
                        codigoSucursal,
                        codigoVigilante,
                        fechaStr
                );

                UIHelper.imprimirExito(
                        "El contrato fue eliminado correctamente."
                );
            } catch (Exception e) {
                UIHelper.imprimirError(e.getMessage());
            }
        } else {
            UIHelper.imprimirMensaje("Eliminación cancelada.");
        }

        UIHelper.pausar();
    }

    /**
     * Solicita un código y elimina una entidad bancaria.
     */
    private void ejecutarEliminarEntidadBancaria() {
        UIHelper.mostrarSubtitulo(
                "ELIMINAR ENTIDAD BANCARIA"
        );

        String codigo = UIHelper.leerTexto(
                "Ingrese el código de la entidad bancaria a eliminar"
        );

        if (UIHelper.leerBooleano(
                "¿Confirma la eliminación de la entidad bancaria?"
        )) {
            try {
                bancarioController.eliminarEntidadBancaria(codigo);

                UIHelper.imprimirExito(
                        "La entidad bancaria fue eliminada correctamente."
                );
            } catch (Exception e) {
                UIHelper.imprimirError(e.getMessage());
            }
        } else {
            UIHelper.imprimirMensaje("Eliminación cancelada.");
        }

        UIHelper.pausar();
    }
}