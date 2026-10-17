package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
 * Representa una banda recibida
 * desde el backend.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BandaDTO {

    private Long id;
    private Integer cantMiembros;
    private String codigo;
    private Boolean activo;

    public BandaDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCantMiembros() {
        return cantMiembros;
    }

    public void setCantMiembros(
            Integer cantMiembros) {

        this.cantMiembros = cantMiembros;
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
