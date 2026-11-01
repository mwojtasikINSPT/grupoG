package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

/*
 * Representa los datos que el frontend envía y recibe
 * al trabajar con un caso judicial.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CasoJudicialDTO {

    private Long id;
    private Boolean sentenciado;
    private String asaltoCodigo;
    private List<String> asaltanteCodigos = new ArrayList<>();
    private String juezCodigo;
    private String codigo;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private String creadoPor;
    private String modificadoPor;

    public CasoJudicialDTO() {
    }

    /*
     * Constructor usado al crear un caso.
     * Su estado inicial lo decide el backend.
     */
    public CasoJudicialDTO(
            String asaltoCodigo,
            List<String> asaltanteCodigos,
            String juezCodigo) {

        this.asaltoCodigo = asaltoCodigo;
        this.asaltanteCodigos = asaltanteCodigos;
        this.juezCodigo = juezCodigo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getSentenciado() {
        return sentenciado;
    }

    public void setSentenciado(Boolean sentenciado) {
        this.sentenciado = sentenciado;
    }

    public String getAsaltoCodigo() {
        return asaltoCodigo;
    }

    public void setAsaltoCodigo(String asaltoCodigo) {
        this.asaltoCodigo = asaltoCodigo;
    }

    public List<String> getAsaltanteCodigos() {
        return asaltanteCodigos;
    }

    public void setAsaltanteCodigos(List<String> asaltanteCodigos) {
        this.asaltanteCodigos = asaltanteCodigos;
    }

    public String getJuezCodigo() {
        return juezCodigo;
    }

    public void setJuezCodigo(String juezCodigo) {
        this.juezCodigo = juezCodigo;
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    public String getModificadoPor() {
        return modificadoPor;
    }

    public void setModificadoPor(String modificadoPor) {
        this.modificadoPor = modificadoPor;
    }
}
