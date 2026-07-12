package controllers;

import daos.UsuarioDAO;
import daos.VigilanteDAO;
import exceptions.*;
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

    /**
     * Inicializa el controlador con las instancias necesarias de los DAOs.
     */
    public UsuariosController() {
        this.usuarioDAO = new UsuarioDAO();
        this.vigilanteDAO = new VigilanteDAO();
    }

    /**
     * Registra un nuevo usuario en el sistema. Si el rol es VIGILANTE, requiere
     * un código de vigilante válido existente en el sistema.
     *
     * @param nombreUsuario Nombre de identificación del usuario.
     * @param password Contraseña de acceso.
     * @param rol Enum que define el tipo de usuario (ADMINISTRADOR,
     * INVESTIGADOR, VIGILANTE).
     * @param codigoVigilante Código asociado al vigilante (solo obligatorio
     * para rol VIGILANTE).
     * @throws Exception Si los datos son inválidos, el nombre de usuario
     * existe, o si el vigilante no existe en el sistema.
     */
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

                    // Ahora busco al vigilante; si no existe, lanzo error directamente
                    Vigilante v = vigilanteDAO.buscarPorId(codigoVigilante);

                    nuevoUsuario = new UsuarioVigilante(nombreUsuario, password, v);
                    break;
            }

            // Guardo el usuario validado
            usuarioDAO.guardar(nuevoUsuario);

        } catch (ErrorAlGuardarException e) {
            throw new Exception("No se pudo guardar el usuario: " + e.getMessage());
        }
    }

    /**
     * Obtiene una lista con todos los usuarios registrados en el sistema.
     *
     * * @return Lista de objetos {@link Usuario}.
     * @throws Exception Si ocurre un error al acceder a la fuente de datos.
     */
    public List<Usuario> listarUsuarios() throws Exception {
        try {
            return usuarioDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception("Error al recuperar la lista de usuarios: " + e.getMessage());
        }
    }

    /**
     * Elimina a un usuario del sistema, validando que el usuario logueado no
     * sea el mismo.
     *
     * @param usuarioAEliminar Nombre del usuario a borrar.
     * @param usuarioLogueado Objeto {@link Usuario} que realiza la operación.
     * @throws Exception Si el usuario a eliminar no existe, o si es la misma
     * cuenta activa.
     */
    public void eliminarUsuario(String usuarioAEliminar, Usuario usuarioLogueado) throws Exception {
        // Validación: comparo el nombre del usuario logueado con el usuario a eliminar
        if (usuarioAEliminar.equalsIgnoreCase(usuarioLogueado.getUsername())) {
            throw new Exception("Por motivos de seguridad, no podés eliminar tu propia cuenta.");
        }
        try {
            // Busco para confirmar existencia
            usuarioDAO.buscarPorId(usuarioAEliminar);
            // Si llega aca, existe
            usuarioDAO.eliminar(usuarioAEliminar);
        } catch (ObjetoNoEncontradoException e) {
            throw new Exception("El usuario '" + usuarioAEliminar + "' no existe en el sistema.");
        } catch (ErrorAlEliminarException e) {
            throw new Exception("No se pudo eliminar el usuario: " + e.getMessage());
        }
    }
}
