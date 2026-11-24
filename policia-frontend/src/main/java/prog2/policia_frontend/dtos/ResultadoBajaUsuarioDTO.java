package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultadoBajaUsuarioDTO {
    private String codigo;
    private int defendidosDesvinculados;
    private String mensaje;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public int getDefendidosDesvinculados() { return defendidosDesvinculados; }
    public void setDefendidosDesvinculados(int defendidosDesvinculados) { this.defendidosDesvinculados = defendidosDesvinculados; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
