package views.submenues;

import controllers.JudicialController;
import controllers.AsaltosController;
import daos.AsaltanteDAO;
import daos.AsaltoDAO;
import daos.BandaDAO;
import daos.SucursalDAO;
import models.CasoJudicial;
import models.Banda;
import models.Asalto;
import views.UIHelper;
import java.util.List;
import models.Rol;

/**
 * Clase encargada de la gestión judicial dentro del sistema. Proporciona un
 * menú interactivo en consola que permite consultar información relacionada con
 * detenidos, bandas delictivas, asaltos registrados y jueces con sus
 * respectivas condenas.
 *
 * Funcionalidades principales: - Consultar detenidos: muestra datos de
 * asaltantes, asaltos y condenas. - Consultar bandas: lista las bandas
 * registradas con su número y cantidad de miembros. - Consultar asaltos:
 * muestra registros de asaltos con identificador, asaltante, sucursal y fecha.
 * - Consultar jueces y condenas: lista expedientes judiciales con juez,
 * asaltante y sentencia.
 *
 * Esta clase actúa como interfaz entre la capa de presentación (UIHelper) y los
 * controladores de negocio (JudicialController y AsaltosController),
 * garantizando que las consultas se realicen de manera ordenada y accesible.
 *
 * @author GrupoG
 */
public class GestionJudicial {

    /**
     * Controlador encargado de gestionar las operaciones relacionadas con los
     * asaltos. Se inicializa con los objetos de acceso a datos (DAO) necesarios
     * para interactuar con la banda, los asaltantes y las sucursales.
     */
    //NO va aca, pero es la unica forma de que no se rompa todo por errores en creacion de entidades sin datos completos
    private final AsaltosController asaltosController = new AsaltosController(new BandaDAO(), new AsaltanteDAO(), new SucursalDAO());
    private final JudicialController judicialController = new JudicialController(new AsaltoDAO());

    /**
     * Muestra el menú principal de gestión judicial y procesa las opciones
     * seleccionadas.
     *
     * @param rolUsuario El rol del usuario actual, útil para restringir accesos
     * si fuera necesario.
     */
    public void mostrar(Rol rolUsuario) {
        int opcion;
        do {
            UIHelper.mostrarSubtitulo("GESTIÓN JUDICIAL - CONSULTAS");
            UIHelper.imprimirMensaje("1. Consultar Detenidos\n2. Consultar Bandas\n3. Consultar Asaltos\n4. Consultar Jueces y Condenas\n0. Volver");
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
            }
        } while (opcion != 0);
    }

    /**
     * Recupera y muestra un listado detallado de todos los asaltantes
     * condenados a prisión.
     */
    private void ejecutarListarDetenidos() {
        UIHelper.mostrarSubtitulo("REGISTRO DE DETENIDOS");
        try {
            List<CasoJudicial> lista = judicialController.listarDetenidos();

            if (lista.isEmpty()) {
                UIHelper.imprimirMensaje("No hay detenidos registrados.");
            } else {
                UIHelper.imprimirMensaje("ASALTANTE          | ID ASALTO | FECHA      | MESES\n--------------------------------------------------------------------------");
                for (CasoJudicial c : lista) {
                    System.out.printf("%-18s | %-9s | %-10s | %-5d%n", c.getAsalto().getAsaltante().getNombreCompleto(), c.getAsalto().getIdAsalto(), c.getAsalto().getFecha(), c.getMesesCarcel());
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar();
    }

    /**
     * Muestra el registro de todas las bandas criminales y su cantidad de
     * integrantes.
     */
    private void ejecutarListarBandas() {
        UIHelper.mostrarSubtitulo("LISTADO DE BANDAS");
        try {
            List<Banda> lista = asaltosController.listarBandas();
            if (lista.isEmpty()) {
                UIHelper.imprimirMensaje("No hay bandas registradas.");
            } else {
                for (Banda b : lista) {
                    System.out.printf("%-12s | %-8d%n", b.getNumeroBanda(), b.getCantMiembros());
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar();
    }

    /**
     * Muestra el histórico completo de los asaltos ocurridos y sus datos
     * básicos.
     */
    private void ejecutarListarAsaltos() {
        UIHelper.mostrarSubtitulo("REGISTRO DE ASALTOS");
        try {
            List<Asalto> lista = asaltosController.listarAsaltos();
            if (lista.isEmpty()) {
                UIHelper.imprimirMensaje("No hay asaltos registrados.");
            } else {
                for (Asalto a : lista) {
                    System.out.printf("%-9s | %-16s | %-10s | %-10s%n", a.getIdAsalto(), a.getAsaltante().getNombreCompleto(), a.getSucursal().getCodigo(), a.getFecha());
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar();
    }

    /**
     * Muestra un resumen de los expedientes judiciales vinculando al juez
     * interviniente con el veredicto final.
     */
    private void ejecutarListarJuecesYCondenas() {
        UIHelper.mostrarSubtitulo("JUECES Y CONDENAS");
        try {
            List<CasoJudicial> lista = judicialController.listarCasosJudiciales();
            if (lista.isEmpty()) {
                UIHelper.imprimirMensaje("No hay expedientes.");
            } else {
                for (CasoJudicial c : lista) {
                    System.out.printf("%-17s | %-18s | %-7s | %-5d%n", c.getJuez().getNombre(), c.getAsalto().getAsaltante().getNombreCompleto(), c.isCondenado() ? "SÍ" : "NO", c.getMesesCarcel());
                }
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar();
    }
}
