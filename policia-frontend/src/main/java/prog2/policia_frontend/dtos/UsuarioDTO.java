package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//datos básicos del usuario que el frontend recibe desde el backend.


//permite ignorar campos adicionales q no necesitamos
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsuarioDTO {
//aca no tiene el password xq el back nunca debe devolver la contraseña
    private Long id;
    private String codigo;
    private String nombre;
    private String rol;
    private Boolean activo;

     // el contructor vacio convierte autaticmnt el JSON en un objeto Java.
    public UsuarioDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
