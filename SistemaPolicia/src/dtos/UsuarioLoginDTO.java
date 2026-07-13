package dtos;

/**
 * Objeto de transferencia de datos (DTO) utilizado para transportar las
 * credenciales de acceso (nombre de usuario y contraseña) desde la interfaz de
 * usuario hacia el servicio de autenticación.
 */
public class UsuarioLoginDTO {

    /**
     * El identificador o nombre del usuario que intenta iniciar sesión.
     */
    private String nombreUsuario;

    /**
     * La contraseña proporcionada por el usuario.
     */
    private String password;

    /**
     * Construye un nuevo objeto con las credenciales proporcionadas.
     *
     * @param nombreUsuario El nombre del usuario.
     * @param password La clave de acceso.
     */
    public UsuarioLoginDTO(String nombreUsuario, String password) {
        this.nombreUsuario = nombreUsuario;
        this.password = password;
    }

    /**
     * Obtiene el nombre de usuario ingresado.
     *
     * Obtiene el nombre de usuario ingresado.
     *
     * @return El nombre de usuario.
     */
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    /**
     * Obtiene la contraseña ingresada.
     *
     * Obtiene la contraseña ingresada.
     *
     * @return La contraseña del usuario.
     */
    public String getPassword() {
        return password;
    }
}
