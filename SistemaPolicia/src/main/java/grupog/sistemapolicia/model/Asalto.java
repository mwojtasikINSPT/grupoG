package grupog.sistemapolicia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "asaltos")
public class Asalto {

    @Id
    @Column(name = "id_asalto", length = 20)
    private String idAsalto;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asaltante_clave", nullable = false)
    private Asaltante asaltante;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_codigo", nullable = false)
    private Sucursal sucursal;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    public Asalto() {
    }

    public Asalto(String idAsalto, Asaltante asaltante,
            Sucursal sucursal, LocalDate fecha) {
        this.idAsalto = idAsalto;
        this.asaltante = asaltante;
        this.sucursal = sucursal;
        this.fecha = fecha;
    }

    public String getIdAsalto() {
        return idAsalto;
    }

    public void setIdAsalto(String idAsalto) {
        this.idAsalto = idAsalto;
    }

    public Asaltante getAsaltante() {
        return asaltante;
    }

    public void setAsaltante(Asaltante asaltante) {
        this.asaltante = asaltante;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}
