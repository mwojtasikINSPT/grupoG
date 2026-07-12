package models;

/**
 * Representa a un usuario con perfil de Administrador. Posee acceso total a las
 * configuraciones y gestión del sistema.
 */
public class UsuarioAdministrador extends Usuario {

    // Añado el identificador de versión para la persistencia
    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto.
     */
    public UsuarioAdministrador() {
        super();
    }

    /**
     * Construye un nuevo usuario administrador.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña.
     */
    public UsuarioAdministrador(String username, String password) {
        super(username, password);
    }

    /**
     * Obtiene el rol correspondiente a este tipo de usuario.
     *
     * @return {@link Rol#ADMINISTRADOR}
     */
    @Override
    public Rol obtenerRol() {
        // Retorno el valor del Enum correspondiente a este perfil. 
        return Rol.ADMINISTRADOR;
    }
}
