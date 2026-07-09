package models;

// Defino al usuario con privilegios únicamente de consulta sobre sus propios datos.
public class UsuarioVigilante extends Usuario {

    // Añado el identificador de versión para la persistencia
    private static final long serialVersionUID = 1L;

    // Vinculo directamente el objeto en lugar del String (ID)
    private Vigilante vigilante;

    public UsuarioVigilante() {
        super();
    }
    
    // Constructor principal
    public UsuarioVigilante(String username, String password, Vigilante vigilante) {
        super(username, password); 
        this.vigilante = vigilante;
    }

    // Getters y Setters
    public Vigilante getVigilante() {
        return vigilante;
    }

    public void setVigilante(Vigilante vigilante) {
        this.vigilante = vigilante;
    }

    @Override
    public Rol obtenerRol() {
        // Retorno el valor del Enum correspondiente a este perfil. 
        return Rol.VIGILANTE; 
    }
}