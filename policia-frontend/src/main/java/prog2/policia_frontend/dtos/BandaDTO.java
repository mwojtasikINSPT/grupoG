package prog2.policia_frontend.dtos;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
 * Representa una banda recibida
 * desde el backend.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BandaDTO {

    private Long id;
    private Integer cantMiembros;
    private String codigo;
    private Boolean activo;

    public BandaDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCantMiembros() {
        return cantMiembros;
    }

    public void setCantMiembros(
            Integer cantMiembros) {

        this.cantMiembros = cantMiembros;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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
