
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
@Table(name = "contratos_vigilancia")
public class ContratoVigilancia {

    @Id
    @Column(name = "codigo", length = 20)
    private String codigo;

    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY
    )
    @JoinColumn(
        name = "sucursal_codigo",
        nullable = false
    )
    private Sucursal sucursal;

    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY
    )
    @JoinColumn(
        name = "vigilante_codigo",
        nullable = false
    )
    private Vigilante vigilante;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "con_arma", nullable = false)
    private boolean conArma;

    public ContratoVigilancia() {
    }

    public ContratoVigilancia(
            String codigo,
            Sucursal sucursal,
            Vigilante vigilante,
            LocalDate fecha,
            boolean conArma) {

        this.codigo = codigo;
        this.sucursal = sucursal;
        this.vigilante = vigilante;
        this.fecha = fecha;
        this.conArma = conArma;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public Vigilante getVigilante() {
        return vigilante;
    }

    public void setVigilante(Vigilante vigilante) {
        this.vigilante = vigilante;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public boolean isConArma() {
        return conArma;
    }

    public void setConArma(boolean conArma) {
        this.conArma = conArma;
    }

    @Override
    public String toString() {
        return "Contrato de Vigilancia ["
                + "Código: " + codigo
                + ", Sucursal: "
                + (sucursal != null
                    ? sucursal.getCodigo()
                    : "Desconocida")
                + ", Vigilante: "
                + (vigilante != null
                    ? vigilante.getCodigo()
                    : "Desconocido")
                + ", Fecha: " + fecha
                + ", Con arma: "
                + (conArma ? "Sí" : "No")
                + "]";
    }
}