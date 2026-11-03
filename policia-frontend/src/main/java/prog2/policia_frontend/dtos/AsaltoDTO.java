package prog2.policia_frontend.dtos;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
 * Representa los datos de un asalto
 * enviados o recibidos del backend.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsaltoDTO {

    private Long id;
    private LocalDate fecha;
    private List<String> asaltantesCodigos
            = new ArrayList<>();
    private String sucursalCodigo;
    private String codigo;
    private Boolean activo;

    public AsaltoDTO() {
    }

    public AsaltoDTO(
            LocalDate fecha,
            List<String> asaltantesCodigos,
            String sucursalCodigo) {

        this.fecha = fecha;
        setAsaltantesCodigos(asaltantesCodigos);
        this.sucursalCodigo = sucursalCodigo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public List<String> getAsaltantesCodigos() {
        return asaltantesCodigos;
    }

    public void setAsaltantesCodigos(
            List<String> asaltantesCodigos) {

        this.asaltantesCodigos
                = asaltantesCodigos != null
                        ? new ArrayList<>(asaltantesCodigos)
                        : null;
    }

    public String getSucursalCodigo() {
        return sucursalCodigo;
    }

    public void setSucursalCodigo(
            String sucursalCodigo) {

        this.sucursalCodigo = sucursalCodigo;
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
