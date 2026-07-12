package models;

/**
 * Representa a un usuario con perfil de Investigador. Tiene privilegios de
 * consulta general sobre los datos del sistema.
 */
public class UsuarioInvestigador extends Usuario {

    // Añado el identificador de versión para la persistencia
    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto.
     */
    public UsuarioInvestigador() {
        super();
    }

    /**
     * Construye un nuevo usuario investigador.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña.
     */
    public UsuarioInvestigador(String username, String password) {
        super(username, password);
    }

    /**
     * Obtiene el rol correspondiente a este tipo de usuario.
     *
     * @return {@link Rol#INVESTIGADOR}
     */
    @Override
    public Rol obtenerRol() {
        // Retorno el valor del Enum correspondiente a este perfil. 
        return Rol.INVESTIGADOR;
    }

}
