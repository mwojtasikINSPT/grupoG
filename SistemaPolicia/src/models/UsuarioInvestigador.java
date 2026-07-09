package models;

// Defino al usuario con privilegios de consulta general sobre el sistema.
public class UsuarioInvestigador extends Usuario {

    // Añado el identificador de versión para la persistencia
    private static final long serialVersionUID = 1L;

    public UsuarioInvestigador() {
        super();
    }

    // Constructor principal
    public UsuarioInvestigador(String username, String password) {
        super(username, password);
    }

    @Override
    public Rol obtenerRol() {
        // Retorno el valor del Enum correspondiente a este perfil. 
        return Rol.INVESTIGADOR;
    }

}