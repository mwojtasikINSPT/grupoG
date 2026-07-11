package views;

import views.submenues.GestionOperativa;
import views.submenues.GestionUsuarios;
import models.Rol;
import models.Usuario;
import views.submenues.GestionJudicial;

public class MenuAdministrador {

    public void mostrarMenu(Usuario usuarioLogueado) {
        int opcion;

        do {
            UIHelper.mostrarTitulo("MENÚ DE ADMIN - Hola," + usuarioLogueado.getUsername());

            UIHelper.imprimirMensaje("1. Gestión de Usuarios\n"
                    + "2. Gestión Operativa\n"
                    + "3. Gestión Judicial\n"
                    + "0. Cerrar Sesión / Salir\n"
                    + "----------------------------------------");

            opcion = UIHelper.leerEntero("Seleccione una opción");

            switch (opcion) {
                case 1 ->
                    new GestionUsuarios().mostrar(Rol.ADMINISTRADOR, usuarioLogueado.getUsername());
                case 2 ->
                    new GestionOperativa().mostrar(Rol.ADMINISTRADOR);
                case 3 ->
                    new GestionJudicial().mostrar(Rol.ADMINISTRADOR);
                case 0 ->
                    UIHelper.imprimirMensaje("\n[SISTEMA] Cerrando sesión...");
                default ->
                    UIHelper.imprimirError("Opción no válida.");
            }
        } while (opcion != 0);
    }
}
