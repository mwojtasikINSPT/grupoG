package models;

public class UsuarioAdministrador extends Usuario {

    // Añado el identificador de versión para la persistencia
    private static final long serialVersionUID = 1L;

    public UsuarioAdministrador() {
        super();
    }

    // Constructor principal
    public UsuarioAdministrador(String username, String password) {
        super(username, password);
    }

    @Override
    public Rol obtenerRol() {
        // Retorno el valor del Enum correspondiente a este perfil. 
        return Rol.ADMINISTRADOR; 
    }
}