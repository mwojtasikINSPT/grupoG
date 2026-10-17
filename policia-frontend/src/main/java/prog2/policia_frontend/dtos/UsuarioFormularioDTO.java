
package prog2.policia_frontend.dtos;

/*
 * Datos que el frontend envía al backend
 * para crear o modificar un usuario.
 *
 * La contraseña solamente viaja hacia el backend;
 * nunca debe mostrarse en la tabla.
 */
public class UsuarioFormularioDTO {

    private String nombre;
    private String password;
    private String rol;

    public UsuarioFormularioDTO() {
    }

    public UsuarioFormularioDTO(
            String nombre,
            String password,
            String rol) {

        this.nombre = nombre;
        this.password = password;
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}