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
    
   public Usuario procesarLogin(UsuarioLoginDTO loginDTO) throws Exception {
        // Validar datos vacíos
        if(loginDTO.getNombreUsuario().trim().isEmpty() || loginDTO.getPassword().trim().isEmpty()){
            throw new Exception("El usuario y la contraseña no pueden estar vacíos.");
        }

        try {
            // Busco el usuario en el archivo .txt
            Usuario usuario = usuarioDAO.buscarPorId(loginDTO.getNombreUsuario());

            if(usuario.getPassword().equals(loginDTO.getPassword())){
                // Si coinciden, devolvemos la variable encontrada
                return usuario;
            } else {
                throw new Exception("La contraseña es incorrecta. Intente nuevamente.");
            }

        } catch (ObjetoNoEncontradoException e) {
            throw new Exception("El usuario '" + loginDTO.getNombreUsuario() + "' no está registrado.");
        } catch (ErrorAlLeerException e) {
            throw new Exception("Error en el sistema: " + e.getMessage());
        }
    }
}
