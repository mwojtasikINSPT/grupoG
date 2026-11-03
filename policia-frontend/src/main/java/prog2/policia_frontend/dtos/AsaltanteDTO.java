package prog2.policia_frontend.dtos;
import java.time.LocalDateTime;

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

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private String creadoPor;
    private String modificadoPor;
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime value) { this.fechaCreacion = value; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime value) { this.fechaModificacion = value; }
    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String value) { this.creadoPor = value; }
    public String getModificadoPor() { return modificadoPor; }
    public void setModificadoPor(String value) { this.modificadoPor = value; }
}
