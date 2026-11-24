package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultadoAsignacionAbogadoDTO {
    private String asaltanteCodigo;
    private String abogadoAnteriorCodigo;
    private String abogadoDefensorCodigo;
    private String mensaje;

    public String getAsaltanteCodigo() { return asaltanteCodigo; }
    public void setAsaltanteCodigo(String asaltanteCodigo) { this.asaltanteCodigo = asaltanteCodigo; }
    public String getAbogadoAnteriorCodigo() { return abogadoAnteriorCodigo; }
    public void setAbogadoAnteriorCodigo(String abogadoAnteriorCodigo) { this.abogadoAnteriorCodigo = abogadoAnteriorCodigo; }
    public String getAbogadoDefensorCodigo() { return abogadoDefensorCodigo; }
    public void setAbogadoDefensorCodigo(String abogadoDefensorCodigo) { this.abogadoDefensorCodigo = abogadoDefensorCodigo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
