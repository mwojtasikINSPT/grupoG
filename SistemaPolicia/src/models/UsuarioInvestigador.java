package models;

public class UsuarioInvestigador extends Usuario {

    public UsuarioInvestigador(String username, String password) {
        super(username, password);
    }

    @Override
    public Rol obtenerRol() {
        // Retornamos el valor del Enum. 
        return Rol.INVESTIGADOR;
    }

}
