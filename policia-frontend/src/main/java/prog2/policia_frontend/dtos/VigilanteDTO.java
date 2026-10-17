
package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
 * Representa los datos de un vigilante
 * recibidos o enviados al backend.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VigilanteDTO {

    private Long id;
    private String codigo;
    private String nombre;

    /*
     * Se envía al crear o cambiar la contraseña,
     * pero el backend nunca la devuelve.
     */
    private String password;

    private Integer edad;
    private String rol;
    private String motivoBaja;
    private Boolean activo;

    public VigilanteDTO() {
    }

    /*
     * Constructor utilizado para crear
     * o modificar un vigilante.
     */
    public VigilanteDTO(
            String nombre,
            String password,
            Integer edad) {

        this.nombre = nombre;
        this.password = password;
        this.edad = edad;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getMotivoBaja() {
        return motivoBaja;
    }

    public void setMotivoBaja(
            String motivoBaja) {

        this.motivoBaja = motivoBaja;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
