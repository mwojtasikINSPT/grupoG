package views;

import controllers.LoginController;
import controllers.MenuController;
import dtos.UsuarioLoginDTO;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import models.Usuario;

/**
 * Punto de entrada del sistema de seguridad bancaria.
 *
 * Coordina el inicio de sesión y delega la selección del menú al
 * {@link MenuController}.
 *
 * @author GrupoG
 */
public class Main {

    /**
     * Inicia la aplicación y mantiene disponible el acceso al sistema hasta
     * que el usuario decide salir.
     *
     * @param args argumentos recibidos al ejecutar la aplicación
     */
    public static void main(String[] args) {
        System.setOut(
                new PrintStream(System.out, true, StandardCharsets.UTF_8)
        );

        LoginController loginController = new LoginController();
        MenuController menuController = new MenuController();

        while (true) {
            UIHelper.mostrarTitulo("SISTEMA DE SEGURIDAD BANCARIA");
            UIHelper.imprimirMensaje(
                    "Por favor, inicie sesión para continuar.\n"
            );

            Usuario usuarioLogueado = solicitarLogin(loginController);

            if (usuarioLogueado == null) {
                UIHelper.imprimirMensaje(
                        "\nCerrando aplicación. ¡Hasta luego!"
                );
                return;
            }

            menuController.arrancarMenuPorRol(usuarioLogueado);
        }
    }

    /**
     * Solicita las credenciales hasta autenticar un usuario o recibir la
     * opción de salida.
     *
     * @param loginController controlador encargado de validar las credenciales
     * @return usuario autenticado o {@code null} si se solicitó salir
     */
    private static Usuario solicitarLogin(LoginController loginController) {
        while (true) {
            UIHelper.mostrarSubtitulo(
                    "INICIO DE SESIÓN (0: Salir del Sistema)"
            );

            String username = UIHelper.leerTexto("Usuario");

            if ("0".equals(username)) {
                return null;
            }

            String password = UIHelper.leerTexto("Contraseña");
            UsuarioLoginDTO loginDTO =
                    new UsuarioLoginDTO(username, password);

            try {
                return loginController.procesarLogin(loginDTO);
            } catch (Exception e) {
                UIHelper.imprimirError(
                        "\n[LOGIN FALLIDO] " + e.getMessage()
                );
                UIHelper.imprimirMensaje("Intente nuevamente.");
            }
        }
    }
}