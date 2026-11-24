package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PresentacionPruebaDefensaDTO {
    private String asaltanteCodigo;
    private String casoCodigo;
    private String descripcion;

    public String getAsaltanteCodigo() { return asaltanteCodigo; }
    public void setAsaltanteCodigo(String asaltanteCodigo) { this.asaltanteCodigo = asaltanteCodigo; }
    public String getCasoCodigo() { return casoCodigo; }
    public void setCasoCodigo(String casoCodigo) { this.casoCodigo = casoCodigo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
