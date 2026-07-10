package views;

// Punto de entrada del programa. Inicializa servicios y muestra el login.

import controllers.LoginController;
import dtos.UsuarioLoginDTO;

public class Main {

    public static void main(String[] args) {
        LoginController loginController = new LoginController();
        UIHelper.mostrarTitulo("SISTEMA DE SEGURIDAD BANCARIA");
        UIHelper.imprimirMensaje("Por favor, inicie sesión para continuar.");
        
        boolean loginExitoso = false;
        
        //Bucle login, insiste hasta que ponga los datos correctos.
        while(!loginExitoso){
            System.out.println("\n--- INICIO DE SESIÓN ---");
            String username = UIHelper.leerTexto("Usuario");
            String password = UIHelper.leerTexto("Contraseña");
            
            //Armamos el DTO
            UsuarioLoginDTO loginDTO = new UsuarioLoginDTO(username, password);
        
            try{
                //el controlador procesa el login. si sale bien llama al menuController
                loginController.procesarLogin(loginDTO);
                loginExitoso = true;
            }catch(Exception e){
                System.out.println("\n[LOGIN FALLIDO]" + e.getMessage());
                System.out.println("Intente nuevamente.");
            }
        }
    }
}
