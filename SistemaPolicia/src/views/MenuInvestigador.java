package views;

import views.submenues.GestionUsuarios;
import views.submenues.GestionOperativa;
import views.submenues.GestionJudicial;
import models.Rol;
import models.Usuario;

public class MenuInvestigador {

    public void mostrarMenu(Usuario usuarioLogueado) {
        int opcion;
        do {
            UIHelper.mostrarTitulo("Menú de Investigador - Hola, " + usuarioLogueado.getUsername());
            UIHelper.imprimirMensaje("1. Gestión de Usuarios\n2. Gestión Operativa\n3. Gestión Judicial\n0. Salir");
            
            opcion = UIHelper.leerEntero("Seleccione una opción");

            switch (opcion) {
                // Al pasar ROL.INVESTIGADOR, las clases ya bloquean las opciones de escritura
                case 1 -> new GestionUsuarios().mostrar(Rol.INVESTIGADOR, usuarioLogueado.getUsername());
                case 2 -> new GestionOperativa().mostrar(Rol.INVESTIGADOR);
                case 3 -> new GestionJudicial().mostrar(Rol.INVESTIGADOR);
                case 0 -> UIHelper.imprimirMensaje("Cerrando sesión...");
                default -> UIHelper.imprimirError("Opción no válida.");
            }
        } while (opcion != 0);
    }
}