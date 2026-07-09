package dtos;

// DTO para transportar las credenciales desde la consola al servicio de autenticación.
public class UsuarioLoginDTO {

    private String nombreUsuario;
    private String password;

    public UsuarioLoginDTO(String nombreUsuario, String password) {
        this.nombreUsuario = nombreUsuario;
        this.password = password;
    }

    public String getNombreUsuario() {return nombreUsuario;}

    public String getPassword() {return password;}   
}
