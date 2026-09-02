package views.submenues;

import controllers.AsaltosController;
import controllers.JudicialController;
import java.util.List;
import models.Asalto;
import models.Banda;
import models.CasoJudicial;
import models.Rol;
import views.UIHelper;

/**
 * Presenta las consultas relacionadas con bandas, asaltos
 * y casos judiciales.
 *
 * @author GrupoG
 */
public class GestionJudicial {

    private final AsaltosController asaltosController;
    private final JudicialController judicialController;

    /**
     * Crea la vista con los controladores utilizados por la aplicación.
     */
    public GestionJudicial() {
        this(
                new AsaltosController(),
                new JudicialController()
        );
    }

    /**
     * Crea la vista con los controladores recibidos.
     *
     * @param asaltosController controlador de bandas y asaltos
     * @param judicialController controlador de casos judiciales
     * @throws IllegalArgumentException si algún controlador es nulo
     */
    public GestionJudicial(
            AsaltosController asaltosController,
            JudicialController judicialController) {

        if (asaltosController == null
                || judicialController == null) {

            throw new IllegalArgumentException(
                    "Los controladores judiciales son obligatorios."
            );
        }

        this.asaltosController = asaltosController;
        this.judicialController = judicialController;
    }

    /**
     * Muestra las consultas judiciales disponibles.
     *
     * @param rolUsuario rol del usuario que accede al menú
     */
    public void mostrar(Rol rolUsuario) {
        int opcion;

        do {
            UIHelper.mostrarSubtitulo(
                    "GESTIÓN JUDICIAL - CONSULTAS"
            );

            UIHelper.imprimirMensaje(
                    "1. Consultar Detenidos\n"
                    + "2. Consultar Bandas\n"
                    + "3. Consultar Asaltos\n"
                    + "4. Consultar Jueces y Condenas\n"
                    + "0. Volver"
            );

            opcion = UIHelper.leerEntero("Seleccione opción");

            switch (opcion) {
                case 1 ->
                    ejecutarListarDetenidos();

                case 2 ->
                    ejecutarListarBandas();

                case 3 ->
                    ejecutarListarAsaltos();

                case 4 ->
                    ejecutarListarJuecesYCondenas();

                case 0 -> {
                    // Regresa al menú anterior.
                }

                default ->
                    UIHelper.imprimirError("Opción no válida.");
            }

        } while (opcion != 0);
    }

    /**
     * Muestra los casos que terminaron con una condena.
     */
    private void ejecutarListarDetenidos() {
        UIHelper.mostrarSubtitulo("REGISTRO DE DETENIDOS");

        try {
            List<CasoJudicial> casos =
                    judicialController.listarDetenidos();

            if (casos.isEmpty()) {
                UIHelper.imprimirMensaje(
                        "No hay detenidos registrados."
                );
            } else {
                UIHelper.imprimirMensaje(
                        "ID ASALTO | ID JUEZ | MESES\n"
                        + "--------------------------------"
                );

                for (CasoJudicial caso : casos) {
                    System.out.printf(
                            "%-9s | %-7s | %-5d%n",
                            caso.getIdAsalto(),
                            caso.getIdJuez(),
                            caso.getMesesCarcel()
                    );
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }

        UIHelper.pausar();
    }

    /**
     * Muestra las bandas registradas.
     */
    private void ejecutarListarBandas() {
        UIHelper.mostrarSubtitulo("LISTADO DE BANDAS");

        try {
            List<Banda> bandas =
                    asaltosController.listarBandas();

            if (bandas.isEmpty()) {
                UIHelper.imprimirMensaje(
                        "No hay bandas registradas."
                );
            } else {
                for (Banda banda : bandas) {
                    System.out.printf(
                            "%-12s | %-8d%n",
                            banda.getNumeroBanda(),
                            banda.getCantMiembros()
                    );
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }

        UIHelper.pausar();
    }

    /**
     * Muestra los asaltos registrados.
     */
    private void ejecutarListarAsaltos() {
        UIHelper.mostrarSubtitulo("REGISTRO DE ASALTOS");

        try {
            List<Asalto> asaltos =
                    asaltosController.listarAsaltos();

            if (asaltos.isEmpty()) {
                UIHelper.imprimirMensaje(
                        "No hay asaltos registrados."
                );
            } else {
                UIHelper.imprimirMensaje(
                        "ID ASALTO | ID ASALTANTE | "
                        + "ID SUCURSAL | FECHA"
                );

                for (Asalto asalto : asaltos) {
                    System.out.printf(
                            "%-9s | %-12s | %-11s | %-10s%n",
                            asalto.getIdAsalto(),
                            asalto.getIdAsaltante(),
                            asalto.getIdSucursal(),
                            asalto.getFecha()
                    );
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }

        UIHelper.pausar();
    }

    /**
     * Muestra los casos judiciales y sus condenas.
     */
    private void ejecutarListarJuecesYCondenas() {
        UIHelper.mostrarSubtitulo("JUECES Y CONDENAS");

        try {
            List<CasoJudicial> casos =
                    judicialController.listarCasosJudiciales();

            if (casos.isEmpty()) {
                UIHelper.imprimirMensaje("No hay expedientes.");
            } else {
                UIHelper.imprimirMensaje(
                        "ID JUEZ | ID ASALTO | CONDENA | MESES"
                );

                for (CasoJudicial caso : casos) {
                    System.out.printf(
                            "%-7s | %-9s | %-7s | %-5d%n",
                            caso.getIdJuez(),
                            caso.getIdAsalto(),
                            caso.isCondenado() ? "SÍ" : "NO",
                            caso.getMesesCarcel()
                    );
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }

        UIHelper.pausar();
    }
}