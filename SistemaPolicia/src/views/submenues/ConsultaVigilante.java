package views.submenues;

import controllers.BancarioController;
import exceptions.ObjetoNoEncontradoException;
import models.ContratoVigilancia;
import models.Vigilante;
import views.UIHelper;
import java.util.List;

public class ConsultaVigilante {

    private final BancarioController bancarioController = new BancarioController();

    public void mostrar(String miCodigo) {
        int opcion;
        do {
            UIHelper.mostrarSubtitulo("MENÚ DE VIGILANTE");
            UIHelper.imprimirMensaje("1. Consultar mis datos personales\n2. Consultar mis contratos\n0. Volver");
            opcion = UIHelper.leerEntero("Seleccione opción");

            switch (opcion) {
                case 1 ->
                    ejecutarVerMisDatos(miCodigo);
                case 2 ->
                    ejecutarVerMisContratos(miCodigo);
                case 0 ->
                    UIHelper.imprimirMensaje("Cerrando sesión...");
                default ->
                    UIHelper.imprimirError("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private void ejecutarVerMisDatos(String miCodigo) {
    UIHelper.mostrarSubtitulo("MIS DATOS PERSONALES");
    try {
        //Buscar al vigilante
        Vigilante v = bancarioController.buscarVigilantePorId(miCodigo);
        
        if (v.getEdad() == 0) {
            System.out.println("Código: " + v.getCodigo());
            System.out.println("Estado: Pendiente (Faltan datos).");
        } else {
            System.out.println("Código: " + v.getCodigo() + "\nEdad: " + v.getEdad() + " años");
        }
    } catch (ObjetoNoEncontradoException e) {
        // Si no está en vigilantes.txt, avisamos al vigilante
        System.out.println("Código: " + miCodigo);
        System.out.println("Estado: Pendiente de registro - Consulte a ADMIN");
    } catch (Exception e) {
        UIHelper.imprimirError("Error: " + e.getMessage());
    }
    UIHelper.pausar();
}
    

    private void ejecutarVerMisContratos(String miCodigo) {
        UIHelper.mostrarSubtitulo("MIS CONTRATOS");
        try {
            List<ContratoVigilancia> lista = bancarioController.listarContratosVigilancia();
            UIHelper.imprimirMensaje("FECHA CONTRATO | SUCURSAL  | PORTA ARMA\n--------------------------------------------");
            boolean tiene = false;
            for (ContratoVigilancia c : lista) {
                if (c.getVigilante().getCodigo().equals(miCodigo)) {
                    System.out.printf("%-14s | %-9s | %-10s%n", c.getFecha(), c.getSucursal().getCodigo(), c.isConArma() ? "SÍ" : "NO");
                    tiene = true;
                }
            }
            if (!tiene) {
                UIHelper.imprimirMensaje("No tenés contratos asignados.");
            }
        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
        UIHelper.pausar();
    }
}
