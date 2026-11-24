package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PruebaDefensaDTO {
    private Long id;
    private String descripcion;
    private java.time.LocalDateTime fechaPresentacion;
    private String abogadoCodigo;
    private String asaltanteCodigo;
    private String casoCodigo;
    private String nombreArchivo;
    private String tipoContenidoArchivo;
    private Long tamanoArchivo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public java.time.LocalDateTime getFechaPresentacion() { return fechaPresentacion; }
    public void setFechaPresentacion(java.time.LocalDateTime fechaPresentacion) { this.fechaPresentacion = fechaPresentacion; }
    public String getAbogadoCodigo() { return abogadoCodigo; }
    public void setAbogadoCodigo(String abogadoCodigo) { this.abogadoCodigo = abogadoCodigo; }
    public String getAsaltanteCodigo() { return asaltanteCodigo; }
    public void setAsaltanteCodigo(String asaltanteCodigo) { this.asaltanteCodigo = asaltanteCodigo; }
    public String getCasoCodigo() { return casoCodigo; }
    public void setCasoCodigo(String casoCodigo) { this.casoCodigo = casoCodigo; }
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    public String getTipoContenidoArchivo() { return tipoContenidoArchivo; }
    public void setTipoContenidoArchivo(String tipoContenidoArchivo) { this.tipoContenidoArchivo = tipoContenidoArchivo; }
    public Long getTamanoArchivo() { return tamanoArchivo; }
    public void setTamanoArchivo(Long tamanoArchivo) { this.tamanoArchivo = tamanoArchivo; }
}
