
package prog2.policia_frontend.dtos;
import java.time.LocalDateTime;

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

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private String creadoPor;
    private String modificadoPor;
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime value) { this.fechaCreacion = value; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime value) { this.fechaModificacion = value; }
    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String value) { this.creadoPor = value; }
    public String getModificadoPor() { return modificadoPor; }
    public void setModificadoPor(String value) { this.modificadoPor = value; }
}