package controllers;

import daos.UsuarioDAO;
import daos.VigilanteDAO;
import exceptions.ErrorAlEliminarException;
import exceptions.ErrorAlGuardarException;
import exceptions.ErrorAlLeerException;
import exceptions.ObjetoNoEncontradoException;
import java.util.List;
import models.Rol;
import models.Usuario;
import models.UsuarioAdministrador;
import models.UsuarioInvestigador;
import models.UsuarioVigilante;
import models.Vigilante;

public class UsuariosController {

    private final UsuarioDAO usuarioDAO;
    private final VigilanteDAO vigilanteDAO;

    public UsuariosController() {
        this.usuarioDAO = new UsuarioDAO();
        this.vigilanteDAO = new VigilanteDAO();
    }

    //Registra un nuevo usuario en .txt, asignando rol desde Enum (no String)    
    public void registrarUsuario(String nombreUsuario, String password, Rol rol, String codigoVigilante) throws Exception {
        // Valido datos no vacíos
        if (nombreUsuario.trim().isEmpty() || password.trim().isEmpty()) {
            throw new Exception("El nombre de usuario y la contraseña no pueden estar vacíos.");
        }

        // Valido que el rol no sea nulo
        if (rol == null) {
            throw new Exception("El rol del usuario es obligatorio.");
        }

        // Valido nombre de usuario único
        try {
            usuarioDAO.buscarPorId(nombreUsuario);
            throw new Exception("El nombre de usuario '" + nombreUsuario + "' ya está en uso.");
        } catch (ObjetoNoEncontradoException e) {
            // Si salta esta excepción significa que no lo encontró, entonces se puede usar
        }

        try {
            // Creo el usuario dependiendo del rol recibido
            Usuario nuevoUsuario = null;

            switch (rol) {
                case ADMINISTRADOR:
                    nuevoUsuario = new UsuarioAdministrador(nombreUsuario, password);
                    break;
                case INVESTIGADOR:
                    nuevoUsuario = new UsuarioInvestigador(nombreUsuario, password);
                    break;
                case VIGILANTE:
                    if (codigoVigilante == null || codigoVigilante.trim().isEmpty()) {
                        throw new Exception("El rol VIGILANTE requiere especificar su código.");
                    }

                    Vigilante v;
                    try {
                        // Busco si ya existe
                        v = vigilanteDAO.buscarPorId(codigoVigilante);
                    } catch (ObjetoNoEncontradoException e) {
                        // Si no existe, creamos uno "en blanco" para que el usuario pueda crearse
                        v = new Vigilante(codigoVigilante, 0);
                    }
                    nuevoUsuario = new UsuarioVigilante(nombreUsuario, password, v);
                    break;
            }

            // Guardo el usuario validado
            usuarioDAO.guardar(nuevoUsuario);

        } catch (ErrorAlGuardarException e) {
            throw new Exception("No se pudo guardar el usuario: " + e.getMessage());
        }
    }

    // listar todos los usuarios
    public List<Usuario> listarUsuarios() throws Exception {
        try {
            return usuarioDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception("Error al recuperar la lista de usuarios: " + e.getMessage());
        }
    }

    // eliminar usuario mediante su nombre de usuario
    public void eliminarUsuario(String usuarioAEliminar, String usuarioLogueado) throws Exception {
        // Evito que el administrador se elimine a sí mismo 
        if (usuarioAEliminar.equalsIgnoreCase(usuarioLogueado)) {
            throw new Exception("Por motivos de seguridad, no podés eliminar tu propia cuenta.");
        }

        try {
            usuarioDAO.buscarPorId(usuarioAEliminar);
            usuarioDAO.eliminar(usuarioAEliminar);
        } catch (ObjetoNoEncontradoException e) {
            throw new Exception("El usuario '" + usuarioAEliminar + "' no existe en el sistema.");
        } catch (ErrorAlEliminarException e) {
            throw new Exception("No se pudo eliminar el usuario: " + e.getMessage());
        }
    }
}
