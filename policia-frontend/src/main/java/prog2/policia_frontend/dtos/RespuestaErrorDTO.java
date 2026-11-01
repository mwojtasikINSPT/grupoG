package prog2.policia_frontend.dtos;

/**
 * Representa la respuesta uniforme de error enviada por el backend.
 */
public class RespuestaErrorDTO {

    private String codigo;
    private String mensaje;

    public RespuestaErrorDTO() {
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
