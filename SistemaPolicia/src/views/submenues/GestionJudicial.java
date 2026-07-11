package views.submenues;

import controllers.JudicialController;
import controllers.AsaltosController;
import models.CasoJudicial;
import models.Banda;
import models.Asalto;
import views.UIHelper;
import java.util.List;

public class GestionJudicial {
    private final JudicialController judicialController = new JudicialController();
    private final AsaltosController asaltosController = new AsaltosController();

    public void mostrar() {
        int opcion;
        do {
            UIHelper.mostrarSubtitulo("GESTIÓN JUDICIAL - CONSULTAS");
            UIHelper.imprimirMensaje("1. Consultar Detenidos\n2. Consultar Bandas\n3. Consultar Asaltos\n4. Consultar Jueces y Condenas\n0. Volver");
            opcion = UIHelper.leerEntero("Seleccione opción");

            switch (opcion) {
                case 1 -> ejecutarListarDetenidos();
                case 2 -> ejecutarListarBandas();
                case 3 -> ejecutarListarAsaltos();
                case 4 -> ejecutarListarJuecesYCondenas();
            }
        } while (opcion != 0);
    }

    // Métodos de listado 
    private void ejecutarListarDetenidos() {
        UIHelper.mostrarSubtitulo("REGISTRO DE DETENIDOS");
        try {
            List<CasoJudicial> lista = judicialController.listarDetenidos();
            if (lista.isEmpty()) UIHelper.imprimirMensaje("No hay detenidos registrados.");
            else {
                UIHelper.imprimirMensaje("ASALTANTE          | ID ASALTO | FECHA      | MESES\n--------------------------------------------------------------------------");
                for (CasoJudicial c : lista) {
                    System.out.printf("%-18s | %-9s | %-10s | %-5d%n", c.getAsalto().getAsaltante().getNombreCompleto(), c.getAsalto().getIdAsalto(), c.getAsalto().getFecha(), c.getMesesCarcel());
                }
            }
        } catch (Exception e) { UIHelper.imprimirError(e.getMessage()); }
        UIHelper.pausar();
    }

    private void ejecutarListarBandas() {
        UIHelper.mostrarSubtitulo("LISTADO DE BANDAS");
        try {
            List<Banda> lista = asaltosController.listarBandas();
            if (lista.isEmpty()) UIHelper.imprimirMensaje("No hay bandas registradas.");
            else {
                for (Banda b : lista) System.out.printf("%-12s | %-8d%n", b.getNumeroBanda(), b.getCantMiembros());
            }
        } catch (Exception e) { UIHelper.imprimirError(e.getMessage()); }
        UIHelper.pausar();
    }

    private void ejecutarListarAsaltos() {
        UIHelper.mostrarSubtitulo("REGISTRO DE ASALTOS");
        try {
            List<Asalto> lista = asaltosController.listarAsaltos();
            if (lista.isEmpty()) UIHelper.imprimirMensaje("No hay asaltos registrados.");
            else {
                for (Asalto a : lista) System.out.printf("%-9s | %-16s | %-10s | %-10s%n", a.getIdAsalto(), a.getAsaltante().getNombreCompleto(), a.getSucursal().getCodigo(), a.getFecha());
            }
        } catch (Exception e) { UIHelper.imprimirError(e.getMessage()); }
        UIHelper.pausar();
    }

    private void ejecutarListarJuecesYCondenas() {
        UIHelper.mostrarSubtitulo("JUECES Y CONDENAS");
        try {
            List<CasoJudicial> lista = judicialController.listarCasosJudiciales();
            if (lista.isEmpty()) UIHelper.imprimirMensaje("No hay expedientes.");
            else {
                for (CasoJudicial c : lista) System.out.printf("%-17s | %-18s | %-7s | %-5d%n", c.getJuez().getNombre(), c.getAsalto().getAsaltante().getNombreCompleto(), c.isCondenado() ? "SÍ" : "NO", c.getMesesCarcel());
            }
        } catch (Exception e) { UIHelper.imprimirError(e.getMessage()); }
        UIHelper.pausar();
    }
}