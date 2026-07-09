package models;

public class UsuarioVigilante extends Usuario {
    private String codigoVigilante;

    public UsuarioVigilante(String username, String password, String codigoVigilante) {
        super(username, password); 
        this.codigoVigilante = codigoVigilante;
    }

    public String getCodigoVigilante() {
        return codigoVigilante;
    }

    @Override
    public Rol obtenerRol() {
        // Retornamos el valor del Enum. 
        return Rol.VIGILANTE; 
    }
}