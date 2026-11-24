package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AsignacionAbogadoDTO {
    private String abogadoCodigo;

    public String getAbogadoCodigo() { return abogadoCodigo; }
    public void setAbogadoCodigo(String abogadoCodigo) { this.abogadoCodigo = abogadoCodigo; }
}
