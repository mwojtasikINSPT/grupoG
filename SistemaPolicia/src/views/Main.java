package views;

import controllers.LoginController;
import dtos.UsuarioLoginDTO;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import models.Usuario;


// Punto de entrada del programa. Inicializa servicios y muestra el login.
public class Main {

    public static void main(String[] args) throws UnsupportedEncodingException {

        System.setOut(new PrintStream(System.out, true, "UTF-8")); //Objeto para q muestre caracteres ASCII, necesita exception

        LoginController loginController = new LoginController();

        // 1. General - Si un usuario cierra sesión, el sistema vuelve a esta pantalla.
        while (true) {
            UIHelper.mostrarTitulo("SISTEMA DE SEGURIDAD BANCARIA");
            UIHelper.imprimirMensaje("Por favor, inicie sesión para continuar.\n");

            Usuario usuarioLogueado = null;
            boolean loginExitoso = false;

            // 2. Verifico credenciales válidas
            while (!loginExitoso) {
                UIHelper.mostrarSubtitulo(" INICIO DE SESIÓN");
                String username = UIHelper.leerTexto("Usuario (0 para Salir del Sistema)");

                // Verifico si quiere salir
                if (username.equals("0")) {
                    UIHelper.imprimirMensaje("\nCerrando aplicación. ¡Hasta luego!");
                    return;
                }

                String password = UIHelper.leerTexto("Contraseña");

                UsuarioLoginDTO loginDTO = new UsuarioLoginDTO(username, password);

                try {
                    // El controlador verifica y nos devuelve quién es
                    usuarioLogueado = loginController.procesarLogin(loginDTO);
                    loginExitoso = true;
                } catch (Exception e) {
                    UIHelper.imprimirError("\n[LOGIN FALLIDO] " + e.getMessage());
                    UIHelper.imprimirMensaje("Intente nuevamente.");
                }
            }

            // 3. Leo el rol y le doy la vista que corresponde
            switch (usuarioLogueado.obtenerRol()) {
                case ADMINISTRADOR:
                    MenuAdministrador menuAdmin = new MenuAdministrador();
                    menuAdmin.mostrarMenu(usuarioLogueado);
                    break;

                case INVESTIGADOR:
                    MenuInvestigador menuInvestigador = new MenuInvestigador();
                    menuInvestigador.mostrarMenu(usuarioLogueado);
                    break;

                case VIGILANTE:
                    MenuVigilante menuVigilante = new MenuVigilante();
                    menuVigilante.mostrarMenu(usuarioLogueado);
                    break;

                default: //En teoria, jamas llega hasta aca
                    UIHelper.imprimirError("Rol de usuario no reconocido por el sistema.");
                    break;
            }
        }
    }

}
