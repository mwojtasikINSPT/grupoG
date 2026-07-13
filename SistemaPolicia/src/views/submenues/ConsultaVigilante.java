package views.submenues;

import controllers.BancarioController;
import exceptions.ObjetoNoEncontradoException;
import models.ContratoVigilancia;
import models.Vigilante;
import views.UIHelper;
import java.util.List;

/**
 * Clase que gestiona las consultas disponibles para un vigilante en el sistema.
 * Permite al vigilante visualizar sus datos personales y los contratos de
 * vigilancia que tiene asignados, a través de un menú interactivo en consola.
 *
 * Funcionalidades principales: - Consultar datos personales: muestra código y
 * edad del vigilante, o estado pendiente si faltan datos. - Consultar
 * contratos: lista los contratos asociados, incluyendo fecha, sucursal y si
 * porta arma.
 *
 * Esta clase actúa como interfaz entre la capa de presentación (UIHelper) y el
 * controlador bancario (BancarioController), brindando acceso restringido a la
 * información relevante para cada vigilante.
 *
 * @author GrupoG
 *
 */
public class ConsultaVigilante {
    
    //Ver - Inyectar dependencias en lugar de instanciar controladores cada vez
    private final BancarioController bancarioController = new BancarioController();

    /**
     * Muestra el menú principal de consultas para el vigilante.
     *
     * @param miCodigo El identificador único del vigilante logueado.
     */
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
                case 0 -> {
                    UIHelper.limpiarPantalla();
                    UIHelper.imprimirMensaje("Sesión cerrada.\nVolviendo al Login...");
                    UIHelper.pausar();
                }
                default ->
                    UIHelper.imprimirError("Opción no válida.");
            }
        } while (opcion != 0);
    }

    /**
     * Recupera y muestra la información personal del vigilante desde el
     * sistema.
     *
     * @param miCodigo Código del vigilante a consultar.
     */
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

    /**
     * Filtra y despliega los contratos de vigilancia asociados al código del
     * vigilante logueado.
     *
     * @param miCodigo Código del vigilante cuyos contratos se desean
     * visualizar.
     */
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
