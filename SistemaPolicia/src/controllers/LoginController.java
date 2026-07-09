package controllers;

import daos.UsuarioDAO;
import dtos.UsuarioLoginDTO;
import exceptions.ErrorAlLeerException;
import exceptions.ObjetoNoEncontradoException;
import models.Usuario;

public class LoginController {

    private final UsuarioDAO usuarioDAO;
    private final MenuController menuController;
    
    public LoginController(){
        this.usuarioDAO = new UsuarioDAO();
        this.menuController = new MenuController();
    }
    
    public void procesarLogin(UsuarioLoginDTO loginDTO) throws Exception{
        //validar datos vacios
        if(loginDTO.getNombreUsuario().trim().isEmpty() || loginDTO.getPassword().trim().isEmpty()){
            throw new Exception("El usuario y la contraseña no pueden estar vacíos.");
        }
    
        //Hacemos la busqueda con el try catch
        try{
            //Buscar el usuario en el archivo .txt
            Usuario usuario = usuarioDAO.buscarPorId(loginDTO.getNombreUsuario());
            
            if(usuario.getPassword().equals(loginDTO.getPassword())){
                //Si coinciden
                // armar el pase para menuController
            }else{
                throw new Exception("La contraseña es incorrecta. Intente nuevamente");
            }
        
        }catch(ObjetoNoEncontradoException e){
            throw new Exception("El usuario '" + loginDTO.getNombreUsuario() + "' no esta registrado.");
        }catch (ErrorAlLeerException e){
            throw new Exception("Error en el sistema: " + e.getMessage());
        }
    }
}
