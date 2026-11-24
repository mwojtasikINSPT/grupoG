package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DefendidoDTO {
    private String codigo;
    private String nombre;
    private Boolean activo;
    private String motivoBaja;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public String getMotivoBaja() { return motivoBaja; }
    public void setMotivoBaja(String motivoBaja) { this.motivoBaja = motivoBaja; }
}
