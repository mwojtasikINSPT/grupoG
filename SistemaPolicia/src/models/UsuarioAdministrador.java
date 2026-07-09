package models;


public class UsuarioAdministrador extends Usuario {

    public UsuarioAdministrador(String username, String password) {
        super(username, password);
    }

    

    @Override
   public Rol obtenerRol() {
        // Retornamos el valor del Enum. 
        return Rol.ADMINISTRADOR; 
    }

}
