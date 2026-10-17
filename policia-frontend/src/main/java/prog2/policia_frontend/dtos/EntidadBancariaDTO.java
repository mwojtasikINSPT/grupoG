
package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
 * Representa una entidad bancaria
 * recibida o enviada al backend.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntidadBancariaDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String domicilioCentral;
    private String motivoBaja;
    private Boolean activo;

    public EntidadBancariaDTO() {
    }

    public EntidadBancariaDTO(
            String nombre,
            String domicilioCentral) {

        this.nombre = nombre;
        this.domicilioCentral = domicilioCentral;
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

    public String getDomicilioCentral() {
        return domicilioCentral;
    }

    public void setDomicilioCentral(
            String domicilioCentral) {

        this.domicilioCentral = domicilioCentral;
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