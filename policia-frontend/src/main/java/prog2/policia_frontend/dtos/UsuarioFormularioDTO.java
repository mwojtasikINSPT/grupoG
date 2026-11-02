package prog2.policia_frontend.dtos;

import java.time.LocalDate;

public class UsuarioFormularioDTO {

    private String nombre;
    private String password;
    private String rol;
    private LocalDate fechaNacimiento;

    public UsuarioFormularioDTO() {
    }

    public UsuarioFormularioDTO(String nombre, String password, String rol, LocalDate fechaNacimiento) {
        this.nombre = nombre;
        this.password = password;
        this.rol = rol;
        this.fechaNacimiento = fechaNacimiento;
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

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
}
