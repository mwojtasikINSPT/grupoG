package controllers;

import daos.IGenericDAO;
import daos.UsuarioDAO;
import dtos.UsuarioLoginDTO;
import exceptions.ErrorAlLeerException;
import exceptions.ObjetoNoEncontradoException;
import models.Usuario;

/**
 * Gestiona la autenticación de los usuarios del sistema.
 * Utiliza una interfaz genérica para no depender del tipo de almacenamiento.
 *
 * @author GrupoG
 */
public class LoginController {

    private final IGenericDAO<Usuario> usuarioDAO;

    /**
     * Crea el controlador con la persistencia utilizada actualmente.
     */
    public LoginController() {
        this(new UsuarioDAO());
    }

    /**
     * Crea el controlador con el DAO recibido.
     *
     * @param usuarioDAO acceso a los datos de usuarios
     * @throws IllegalArgumentException si el DAO es nulo
     */
    public LoginController(
            IGenericDAO<Usuario> usuarioDAO) {

        if (usuarioDAO == null) {
            throw new IllegalArgumentException(
                    "El DAO de usuarios es obligatorio."
            );
        }

        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Valida las credenciales y devuelve el usuario autenticado.
     *
     * @param loginDTO credenciales ingresadas
     * @return usuario correspondiente a las credenciales
     * @throws Exception si las credenciales son incorrectas o no puede
     *                   accederse a la información
     */
    public Usuario procesarLogin(
            UsuarioLoginDTO loginDTO) throws Exception {

        validarCredenciales(loginDTO);

        String username = loginDTO
                .getNombreUsuario()
                .trim()
                .toLowerCase();

        try {
            Usuario usuario =
                    usuarioDAO.buscarPorId(username);

            if (!loginDTO.getPassword()
                    .equals(usuario.getPassword())) {

                throw new Exception(
                        "Usuario o contraseña incorrectos."
                );
            }

            return usuario;

        } catch (ObjetoNoEncontradoException e) {
            throw new Exception(
                    "Usuario o contraseña incorrectos."
            );

        } catch (ErrorAlLeerException e) {
            throw new Exception(
                    "Error al consultar los usuarios: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Comprueba que las credenciales tengan valores válidos.
     *
     * @param loginDTO credenciales que se validarán
     * @throws Exception si falta el usuario o la contraseña
     */
    private void validarCredenciales(
            UsuarioLoginDTO loginDTO) throws Exception {

        if (loginDTO == null
                || loginDTO.getNombreUsuario() == null
                || loginDTO.getNombreUsuario().trim().isEmpty()
                || loginDTO.getPassword() == null
                || loginDTO.getPassword().trim().isEmpty()) {

            throw new Exception(
                    "Usuario o contraseña incorrectos."
            );
        }
    }
}