package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CasoDefensaDTO {
    private String codigo;
    private String defendidoCodigo;
    private String asaltoCodigo;
    private java.time.LocalDate fechaAsalto;
    private String juezCodigo;
    private Boolean activo;
    private String estado;
    private String situacionJudicial;
    private Boolean sentenciado;
    private Boolean condenado;
    private Integer tiempoCarcel;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDefendidoCodigo() { return defendidoCodigo; }
    public void setDefendidoCodigo(String defendidoCodigo) { this.defendidoCodigo = defendidoCodigo; }
    public String getAsaltoCodigo() { return asaltoCodigo; }
    public void setAsaltoCodigo(String asaltoCodigo) { this.asaltoCodigo = asaltoCodigo; }
    public java.time.LocalDate getFechaAsalto() { return fechaAsalto; }
    public void setFechaAsalto(java.time.LocalDate fechaAsalto) { this.fechaAsalto = fechaAsalto; }
    public String getJuezCodigo() { return juezCodigo; }
    public void setJuezCodigo(String juezCodigo) { this.juezCodigo = juezCodigo; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getSituacionJudicial() { return situacionJudicial; }
    public void setSituacionJudicial(String situacionJudicial) { this.situacionJudicial = situacionJudicial; }
    public Boolean getSentenciado() { return sentenciado; }
    public void setSentenciado(Boolean sentenciado) { this.sentenciado = sentenciado; }
    public Boolean getCondenado() { return condenado; }
    public void setCondenado(Boolean condenado) { this.condenado = condenado; }
    public Integer getTiempoCarcel() { return tiempoCarcel; }
    public void setTiempoCarcel(Integer tiempoCarcel) { this.tiempoCarcel = tiempoCarcel; }
}
