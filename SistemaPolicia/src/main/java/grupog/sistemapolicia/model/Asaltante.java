package grupog.sistemapolicia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "asaltantes")
public class Asaltante {

    @Id
    @Column(name = "clave", length = 20)
    private String clave;

    @Column(
        name = "nombre_completo",
        length = 100,
        nullable = false
    )
    private String nombreCompleto;

    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY
    )
    @JoinColumn(
        name = "numero_banda",
        nullable = false
    )
    private Banda banda;

    public Asaltante() {
    }

    public Asaltante(
            String clave,
            String nombreCompleto,
            Banda banda) {

        this.clave = clave;
        this.nombreCompleto = nombreCompleto;
        this.banda = banda;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(
            String nombreCompleto) {

        this.nombreCompleto = nombreCompleto;
    }

    public Banda getBanda() {
        return banda;
    }

    public void setBanda(Banda banda) {
        this.banda = banda;
    }

    @Override
    public String toString() {
        return "Asaltante ["
                + "Clave: " + clave
                + ", Nombre: " + nombreCompleto
                + ", Banda: "
                + (banda != null
                    ? banda.getNumeroBanda()
                    : "Sin banda")
                + "]";
    }
}