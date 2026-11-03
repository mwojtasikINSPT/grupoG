package prog2.policia_frontend.dtos;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

/* Representa los datos de un juez. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JuezDTO {

    private Long id;
    private String nombre;
    private LocalDate juezDesde;
    private Integer aniosServicio;
    private String codigo;
    private String motivoBaja;
    private Boolean activo;

    public JuezDTO() {
    }

    public JuezDTO(
            String nombre,
            LocalDate juezDesde) {

        this.nombre = nombre;
        this.juezDesde = juezDesde;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public LocalDate getJuezDesde() { return juezDesde; }
    public void setJuezDesde(LocalDate juezDesde) { this.juezDesde = juezDesde; }
    public Integer getAniosServicio() { return aniosServicio; }
    public void setAniosServicio(Integer aniosServicio) { this.aniosServicio = aniosServicio; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getMotivoBaja() { return motivoBaja; }
    public void setMotivoBaja(String motivoBaja) { this.motivoBaja = motivoBaja; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

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
