package controllers;

import models.Usuario;
//import views.MenuAdministrador;
//import views.MenuInvestigador;
//import views.MenuVigilante;


//muestra menu principal, tiene que tener en cuenta rol de usuairo
public class MenuController {
    
    public void arrancarMenuPorPol(Usuario usuarioLogueado){
    if(usuarioLogueado == null){
        System.out.println("[ERROR] Sesión inválida.");
    }
    
    
    switch(usuarioLogueado.obtenerRol()){
        case ADMINISTRADOR:
            System.out.println("\n [SISTEMA] Abriendo Menú de Administrador...");
            //MenuAdministrador menuAdmin = new MenuAdministrador();
            //menuAdmin.mostrar()
            break;
            
        case INVESTIGADOR: 
            System.out.println("\n [SISTEMA] Abriendo Menú de Investigador...");
            //MenuInvestigador menuInv = new MenuInvestigador();
            //menuInv.mostrar()
            break;
        
        case VIGILANTE:
            System.out.println("\n [SISTEMA] Abriendo Menú de Administrador...");
            //MenuVigilante menuVig = new MenuVigilante();
            //menuVig.mostrar()
            break;
        
        default: 
            System.out.println("[ERROR] El rol del usuario es desconocido.");
            break;
    }
    }
}
