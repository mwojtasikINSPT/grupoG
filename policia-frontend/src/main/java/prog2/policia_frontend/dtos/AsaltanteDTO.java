package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
 * Representa los datos de un asaltante
 * enviados o recibidos del backend.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsaltanteDTO {

    private Long id;
    private String nombre;
    private String bandaCodigo;
    private String codigo;
    private String motivoBaja;
    private Boolean quitarDeBanda;
    private Boolean activo;

    public AsaltanteDTO() {
    }

    public AsaltanteDTO(
            String nombre,
            String bandaCodigo) {

        this.nombre = nombre;
        this.bandaCodigo = bandaCodigo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getBandaCodigo() {
        return bandaCodigo;
    }

    public void setBandaCodigo(
            String bandaCodigo) {

        this.bandaCodigo = bandaCodigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMotivoBaja() {
        return motivoBaja;
    }

    public void setMotivoBaja(
            String motivoBaja) {

        this.motivoBaja = motivoBaja;
    }

    public Boolean getQuitarDeBanda() {
        return quitarDeBanda;
    }

    public void setQuitarDeBanda(
            Boolean quitarDeBanda) {

        this.quitarDeBanda = quitarDeBanda;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
