package controllers;

import daos.UsuarioDAO;
import dtos.UsuarioLoginDTO;
import exceptions.ErrorAlLeerException;
import exceptions.ObjetoNoEncontradoException;
import models.Usuario;

/**
 * Controlador encargado de gestionar el proceso de autenticación de los
 * usuarios en el sistema.
 */
public class LoginController {

    private final UsuarioDAO usuarioDAO;

    /**
     * Inicializa el controlador con los DAOs y componentes necesarios para el
     * login.
     */
    public LoginController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Procesa las credenciales de inicio de sesión de un usuario.
     *
     * @param loginDTO Objeto que contiene las credenciales (usuario y
     * contraseña).
     * 
     * @return El objeto {@link Usuario} autenticado si las credenciales son
     *         correctas.
     * @throws Exception Si el usuario o contraseña son inválidos (mensaje
     *                   genérico por seguridad).
     */
    public Usuario procesarLogin(UsuarioLoginDTO loginDTO) throws Exception {

        // Valido que el DTO no sea nulo
        if (loginDTO == null) {
            throw new Exception("Credenciales no proporcionadas.");
        }

        // Validar datos
        if (loginDTO.getNombreUsuario() == null || loginDTO.getNombreUsuario().trim().isEmpty()
                || loginDTO.getPassword() == null || loginDTO.getPassword().trim().isEmpty()) {
            throw new Exception("Usuario o contraseña incorrectos.");
        }

        try {
            // Normalizamos el nombre ingresado
            String nombreNormalizado = loginDTO.getNombreUsuario().trim().toLowerCase();

            // Busco el usuario en el archivo .txt
            Usuario usuario = usuarioDAO.buscarPorId(nombreNormalizado);

            // Valido contrasena
            if (loginDTO.getPassword().equals(usuario.getPassword())) {
                // Si coinciden, devuelvo el usuario
                return usuario;
            } else {
                throw new Exception("Usuario o contraseña incorrectos.");
            }

        } catch (ObjetoNoEncontradoException e) {
            // Mensaje genérico para no revelar si el usuario existe o no
            throw new Exception("Usuario o contraseña incorrectos.");
        } catch (ErrorAlLeerException e) {
            throw new Exception("Error en el sistema: " + e.getMessage());
        }
    }
}
