package views;

import controllers.LoginController;
import dtos.UsuarioLoginDTO;
import models.Usuario;

// Punto de entrada del programa. Inicializa servicios y muestra el login.
public class Main {

    public static void main(String[] args) {
        LoginController loginController = new LoginController();
        
        // 1. General - Si un usuario cierra sesión, el sistema vuelve a esta pantalla.
        while (true) {
            UIHelper.mostrarTitulo("SISTEMA DE SEGURIDAD BANCARIA");
            UIHelper.imprimirMensaje("Por favor, inicie sesión para continuar.");
            
            Usuario usuarioLogueado = null;
            boolean loginExitoso = false;
            
            // 2. Verificacion credenciales válidas
            while (!loginExitoso) {
                System.out.println("\n--- INICIO DE SESIÓN ---");
                String username = UIHelper.leerTexto("Usuario");
                String password = UIHelper.leerTexto("Contraseña");
                
                UsuarioLoginDTO loginDTO = new UsuarioLoginDTO(username, password);
            
                try {
                    // El controlador verifica y nos devuelve quién es
                    usuarioLogueado = loginController.procesarLogin(loginDTO);
                    loginExitoso = true;
                } catch (Exception e) {
                    System.out.println("\n[LOGIN FALLIDO] " + e.getMessage());
                    System.out.println("Intente nuevamente.");
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
                    
                default:
                    UIHelper.imprimirError("Rol de usuario no reconocido por el sistema.");
                    break;
            }
        }
    }
}