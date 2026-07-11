package views;

import controllers.BancarioController;
import java.util.List;
import models.ContratoVigilancia;
import models.Usuario;
import models.UsuarioVigilante;

// Menu Vigilantes - Solo consulta datos propios.
public class MenuVigilante {

    private final BancarioController bancarioController;

    public MenuVigilante() {
        this.bancarioController = new BancarioController();
    }

    public void mostrarMenu(Usuario usuarioLogueado) {
        
        // Verificamos que el objeto sea de la clase correcta
        if (!(usuarioLogueado instanceof UsuarioVigilante)) {
            UIHelper.imprimirError("Error de sistema: El usuario no tiene perfil de Vigilante.");
            return;
        }

        UsuarioVigilante vigilanteLogueado = (UsuarioVigilante) usuarioLogueado;
        
        // Sacamos el código del vigilante asociado 
        String miCodigo = vigilanteLogueado.getVigilante().getCodigo(); 
        
        int opcion = -1;

        while (opcion != 0) {
            UIHelper.mostrarTitulo("Menú de Vigilante - Hola, " + usuarioLogueado.getUsername());
            UIHelper.imprimirMensaje("--- MI PERFIL ---\n"
                    + "1. Consultar mis datos personales\n"
                    + "2. Consultar mis contratos y asignaciones\n\n"
                    + "0. Cerrar Sesión / Salir\n"
                    + "----------------------------------------");

            opcion = UIHelper.leerEntero("Seleccione una opción");

            switch (opcion) {
                case 1:
                    ejecutarVerMisDatos(miCodigo);
                    break;
                case 2:
                    ejecutarVerMisContratos(miCodigo);
                    break;
                case 0:
                    UIHelper.imprimirMensaje("\n[SISTEMA] Cerrando sesión. Volviendo al login...");
                    break;
                default:
                    UIHelper.imprimirError("Opción no válida. Intente otra vez.");
                    break;
            }
        }
    }

    private void ejecutarVerMisDatos(String miCodigo) {
        UIHelper.mostrarTitulo("MIS DATOS PERSONALES");
        
        try {
            // Traemos la lista completa y buscamos al usuario
            List<models.Vigilante> listaVigilantes = bancarioController.listarVigilantes();
            boolean encontrado = false;

            for (models.Vigilante v : listaVigilantes) {
                if (v.getCodigo().equals(miCodigo)) {
                    System.out.println("Código Identificador : " + v.getCodigo());
                    System.out.println("Edad registrada      : " + v.getEdad() + " años");
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                UIHelper.imprimirError("Tus datos operativos aún no fueron cargados por el Administrador.");
            }

        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
        
        UIHelper.pausar();
    }

    private void ejecutarVerMisContratos(String miCodigo) {
        UIHelper.mostrarTitulo("MIS CONTRATOS Y ASIGNACIONES");
        
        try {
            List<ContratoVigilancia> listaContratos = bancarioController.listarContratosVigilancia();
            boolean tieneContratos = false;

            UIHelper.imprimirMensaje(
                    "--------------------------------------------------------------------------------\n"
                    + "FECHA CONTRATO | SUCURSAL   | DOMICILIO                          | PORTA ARMA\n"
                    + "--------------------------------------------------------------------------------"
            );

            // Recorremos todos los contratos y mostramos solo los que coincidan con su código
            for (ContratoVigilancia c : listaContratos) {
                if (c.getVigilante().getCodigo().equals(miCodigo)) {
                    String llevaArma = c.isConArma() ? "SÍ" : "NO";
                    
                    System.out.printf("%-14s | %-10s | %-34s | %-10s%n",
                            c.getFecha().toString(),
                            c.getSucursal().getCodigo(),
                            c.getSucursal().getDomicilio(),
                            llevaArma);
                            
                    tieneContratos = true;
                }
            }

            UIHelper.imprimirMensaje("--------------------------------------------------------------------------------");

            if (!tieneContratos) {
                System.out.println("\nNo tenés turnos ni contratos asignados en este momento.");
            }

        } catch (Exception e) {
            UIHelper.imprimirError(e.getMessage());
        }
        
        UIHelper.pausar();
    }
}
