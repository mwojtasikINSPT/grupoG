package controllers;

import daos.IGenericDAO;
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

/**
 * Gestiona las operaciones relacionadas con las cuentas de usuario.
 * Utiliza interfaces genéricas para no depender del almacenamiento.
 *
 * @author GrupoG
 */
public class UsuariosController {

    private final IGenericDAO<Usuario> usuarioDAO;
    private final IGenericDAO<Vigilante> vigilanteDAO;

    /**
     * Crea el controlador con los DAO utilizados actualmente.
     */
    public UsuariosController() {
        this(
                new UsuarioDAO(),
                new VigilanteDAO()
        );
    }

    /**
     * Crea el controlador con los DAO recibidos.
     *
     * @param usuarioDAO acceso a los datos de usuarios
     * @param vigilanteDAO acceso a los datos de vigilantes
     * @throws IllegalArgumentException si algún DAO es nulo
     */
    public UsuariosController(
            IGenericDAO<Usuario> usuarioDAO,
            IGenericDAO<Vigilante> vigilanteDAO) {

        if (usuarioDAO == null || vigilanteDAO == null) {
            throw new IllegalArgumentException(
                    "Los DAO de usuarios y vigilantes son obligatorios."
            );
        }

        this.usuarioDAO = usuarioDAO;
        this.vigilanteDAO = vigilanteDAO;
    }

    /**
     * Registra una cuenta con el rol indicado.
     *
     * @param nombreUsuario nombre utilizado para iniciar sesión
     * @param password contraseña de la cuenta
     * @param rol rol asignado
     * @param codigoVigilante código asociado al rol vigilante
     * @throws Exception si los datos son inválidos o no pueden guardarse
     */
    public void registrarUsuario(
            String nombreUsuario,
            String password,
            Rol rol,
            String codigoVigilante) throws Exception {

        validarDatos(nombreUsuario, password, rol);
        verificarNombreDisponible(nombreUsuario);

        Usuario nuevoUsuario = crearUsuario(
                nombreUsuario,
                password,
                rol,
                codigoVigilante
        );

        try {
            usuarioDAO.guardar(nuevoUsuario);
        } catch (ErrorAlGuardarException e) {
            throw new Exception(
                    "No se pudo guardar el usuario: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Obtiene todas las cuentas registradas.
     *
     * @return lista de usuarios
     * @throws Exception si no puede accederse a la información
     */
    public List<Usuario> listarUsuarios() throws Exception {
        try {
            return usuarioDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception(
                    "Error al recuperar la lista de usuarios: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Elimina una cuenta de usuario.
     *
     * @param usuarioAEliminar nombre del usuario que se eliminará
     * @param usuarioLogueado usuario que realiza la operación
     * @throws Exception si los datos son inválidos o no puede eliminarse
     */
    public void eliminarUsuario(
            String usuarioAEliminar,
            Usuario usuarioLogueado) throws Exception {

        if (usuarioAEliminar == null
                || usuarioAEliminar.trim().isEmpty()
                || usuarioLogueado == null) {

            throw new Exception(
                    "Datos de usuario inválidos para realizar "
                    + "la eliminación."
            );
        }

        if (usuarioAEliminar.equalsIgnoreCase(
                usuarioLogueado.getUsername())) {

            throw new Exception(
                    "Por motivos de seguridad, no podés eliminar "
                    + "tu propia cuenta."
            );
        }

        try {
            usuarioDAO.buscarPorId(usuarioAEliminar);
            usuarioDAO.eliminar(usuarioAEliminar);

        } catch (ObjetoNoEncontradoException e) {
            throw new Exception(
                    "El usuario '" + usuarioAEliminar
                    + "' no existe en el sistema."
            );

        } catch (ErrorAlLeerException e) {
            throw new Exception(
                    "No se pudo consultar el usuario: "
                    + e.getMessage()
            );

        } catch (ErrorAlEliminarException e) {
            throw new Exception(
                    "No se pudo eliminar el usuario: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Valida los datos básicos de una cuenta.
     *
     * @param nombreUsuario nombre de la cuenta
     * @param password contraseña de la cuenta
     * @param rol rol asignado
     * @throws Exception si algún dato es inválido
     */
    private void validarDatos(
            String nombreUsuario,
            String password,
            Rol rol) throws Exception {

        if (nombreUsuario == null
                || nombreUsuario.trim().isEmpty()
                || password == null
                || password.trim().isEmpty()) {

            throw new Exception(
                    "El nombre de usuario y la contraseña "
                    + "no pueden estar vacíos."
            );
        }

        if (password.length() < 5) {
            throw new Exception(
                    "La contraseña debe tener al menos 5 caracteres."
            );
        }

        if (rol == null) {
            throw new Exception(
                    "El rol del usuario es obligatorio."
            );
        }
    }

    /**
     * Comprueba que un nombre de usuario esté disponible.
     *
     * @param nombreUsuario nombre que se comprobará
     * @throws Exception si ya existe o no puede consultarse
     */
    private void verificarNombreDisponible(
            String nombreUsuario) throws Exception {

        try {
            usuarioDAO.buscarPorId(nombreUsuario);

            throw new Exception(
                    "El nombre de usuario '" + nombreUsuario
                    + "' ya está en uso."
            );

        } catch (ObjetoNoEncontradoException e) {
            // No encontrarlo indica que el nombre está disponible.

        } catch (ErrorAlLeerException e) {
            throw new Exception(
                    "No se pudo verificar el nombre de usuario: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Construye el tipo de usuario correspondiente al rol.
     *
     * @param nombreUsuario nombre de la cuenta
     * @param password contraseña de la cuenta
     * @param rol rol asignado
     * @param codigoVigilante código del vigilante asociado
     * @return usuario preparado para guardarse
     * @throws Exception si falta el código o el vigilante no existe
     */
    private Usuario crearUsuario(
            String nombreUsuario,
            String password,
            Rol rol,
            String codigoVigilante) throws Exception {

        return switch (rol) {
            case ADMINISTRADOR ->
                new UsuarioAdministrador(
                        nombreUsuario,
                        password
                );

            case INVESTIGADOR ->
                new UsuarioInvestigador(
                        nombreUsuario,
                        password
                );

            case VIGILANTE -> {
                if (codigoVigilante == null
                        || codigoVigilante.trim().isEmpty()) {

                    throw new Exception(
                            "El rol VIGILANTE requiere especificar "
                            + "su código."
                    );
                }

                try {
                    vigilanteDAO.buscarPorId(codigoVigilante);

                    yield new UsuarioVigilante(
                            nombreUsuario,
                            password,
                            codigoVigilante
                    );

                } catch (ObjetoNoEncontradoException e) {
                    throw new Exception(
                            "No existe un vigilante con el código '"
                            + codigoVigilante + "'."
                    );

                } catch (ErrorAlLeerException e) {
                    throw new Exception(
                            "No se pudo consultar el vigilante: "
                            + e.getMessage()
                    );
                }
            }
        };
    }
}