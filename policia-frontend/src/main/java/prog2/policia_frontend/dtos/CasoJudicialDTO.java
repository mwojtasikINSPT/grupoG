package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

/*
 * Representa los datos que el frontend envía y recibe
 * al trabajar con un caso judicial.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CasoJudicialDTO {

    private Long id;
    private Boolean sentenciado;
    private Boolean condenado;
    private Integer tiempoCarcel;
    private String asaltoCodigo;
    private List<String> asaltanteCodigos = new ArrayList<>();
    private String juezCodigo;
    private String codigo;
    private Boolean activo;

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
}
