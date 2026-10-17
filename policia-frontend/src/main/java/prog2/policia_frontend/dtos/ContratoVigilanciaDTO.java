package prog2.policia_frontend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

/*
 * Representa los datos de un contrato
 * de vigilancia enviados o recibidos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContratoVigilanciaDTO {

    private Long id;
    private LocalDate fecha;
    private Boolean conArma;
    private String vigilanteCodigo;
    private String sucursalCodigo;
    private String codigo;
    private Boolean activo;
    private String motivoBaja;

    public ContratoVigilanciaDTO() {
    }

    /*
     * Constructor utilizado al crear
     * o modificar un contrato.
     */
    public ContratoVigilanciaDTO(
            LocalDate fecha,
            Boolean conArma,
            String vigilanteCodigo,
            String sucursalCodigo) {

        this.fecha = fecha;
        this.conArma = conArma;
        this.vigilanteCodigo = vigilanteCodigo;
        this.sucursalCodigo = sucursalCodigo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Boolean getConArma() {
        return conArma;
    }

    public void setConArma(Boolean conArma) {
        this.conArma = conArma;
    }

    public String getVigilanteCodigo() {
        return vigilanteCodigo;
    }

    public void setVigilanteCodigo(
            String vigilanteCodigo) {

        this.vigilanteCodigo = vigilanteCodigo;
    }

    public String getSucursalCodigo() {
        return sucursalCodigo;
    }

    public void setSucursalCodigo(
            String sucursalCodigo) {

        this.sucursalCodigo = sucursalCodigo;
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

    public String getMotivoBaja() {
        return motivoBaja;
    }

    public void setMotivoBaja(
            String motivoBaja) {

        this.motivoBaja = motivoBaja;
    }
}
