package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Datos de la sentencia individual de un asaltante dentro de un caso.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SentenciaAsaltanteDTO {

    private String asaltanteCodigo;
    private Boolean sentenciado;
    private Boolean condenado;
    private Integer tiempoCarcel;

    public SentenciaAsaltanteDTO() {
    }

    public SentenciaAsaltanteDTO(
            String asaltanteCodigo,
            Boolean condenado,
            Integer tiempoCarcel) {

        this.asaltanteCodigo = asaltanteCodigo;
        this.condenado = condenado;
        this.tiempoCarcel = tiempoCarcel;
    }

    public String getAsaltanteCodigo() {
        return asaltanteCodigo;
    }

    public void setAsaltanteCodigo(String asaltanteCodigo) {
        this.asaltanteCodigo = asaltanteCodigo;
    }

    public Boolean getSentenciado() {
        return sentenciado;
    }

    public void setSentenciado(Boolean sentenciado) {
        this.sentenciado = sentenciado;
    }

    public Boolean getCondenado() {
        return condenado;
    }

    public void setCondenado(Boolean condenado) {
        this.condenado = condenado;
    }

    public Integer getTiempoCarcel() {
        return tiempoCarcel;
    }

    public void setTiempoCarcel(Integer tiempoCarcel) {
        this.tiempoCarcel = tiempoCarcel;
    }
}
