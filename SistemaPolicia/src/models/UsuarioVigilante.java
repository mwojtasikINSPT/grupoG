package models;

/**
 * Representa a un usuario con perfil de Vigilante. Contiene una referencia
 * directa al objeto {@link Vigilante} asociado para gestionar sus datos
 * específicos de seguridad.
 */
public class UsuarioVigilante extends Usuario {

    // Añado el identificador de versión para la persistencia
    private static final long serialVersionUID = 1L;

    // Vinculo directamente el objeto en lugar del String (ID)
    private Vigilante vigilante;

    /**
     * Constructor por defecto.
     */
    public UsuarioVigilante() {
        super();
    }

    /**
     * Construye un nuevo usuario vigilante asociado a una entidad de vigilante.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @param vigilante El objeto {@link Vigilante} vinculado a este usuario.
     */
    public UsuarioVigilante(String username, String password, Vigilante vigilante) {
        super(username, password);
        this.vigilante = vigilante;
    }

    // Getters y Setters
    /**
     * Obtiene el vigilante vinculado al usuario.
     *
     * @return El objeto vigilante asociado.
     */
    public Vigilante getVigilante() {
        return vigilante;
    }

    /**
     * Asigna el vigilante vinculado al usuario.
     *
     * @param vigilante El objeto vigilante a vincular.
     */
    public void setVigilante(Vigilante vigilante) {
        this.vigilante = vigilante;
    }

    /**
     * Obtiene el rol correspondiente a este tipo de usuario.
     *
     * @return {@link Rol#VIGILANTE}
     */
    @Override
    public Rol obtenerRol() {
        // Retorno el valor del Enum correspondiente a este perfil. 
        return Rol.VIGILANTE;
    }
}
