package prog2.policia_frontend.dtos;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
 * Representa una sucursal recibida
 * o enviada al backend.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SucursalDTO {

    private Long id;
    private String codigo;
    private String domicilio;
    private Integer cantEmpleados;
    private String entidadBancariaCodigo;
    private String motivoCierre;
    private Boolean activo;

    public SucursalDTO() {
    }

    public SucursalDTO(
            String domicilio,
            Integer cantEmpleados,
            String entidadBancariaCodigo) {

        this.domicilio = domicilio;
        this.cantEmpleados = cantEmpleados;
        this.entidadBancariaCodigo
                = entidadBancariaCodigo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public Integer getCantEmpleados() {
        return cantEmpleados;
    }

    public void setCantEmpleados(
            Integer cantEmpleados) {

        this.cantEmpleados = cantEmpleados;
    }

    public String getEntidadBancariaCodigo() {
        return entidadBancariaCodigo;
    }

    public void setEntidadBancariaCodigo(
            String entidadBancariaCodigo) {

        this.entidadBancariaCodigo
                = entidadBancariaCodigo;
    }

    public String getMotivoCierre() {
        return motivoCierre;
    }

    public void setMotivoCierre(
            String motivoCierre) {

        this.motivoCierre = motivoCierre;
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