package grupog.sistemapolicia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bandas")
public class Banda {

    @Id
    @Column(name = "numero_banda", length = 20)
    private String numeroBanda;

    @Column(
        name = "cantidad_miembros",
        nullable = false
    )
    private int cantMiembros;

    public Banda() {
    }

    public Banda(
            String numeroBanda,
            int cantMiembros) {

        this.numeroBanda = numeroBanda;
        this.cantMiembros = cantMiembros;
    }

    public String getNumeroBanda() {
        return numeroBanda;
    }

    public void setNumeroBanda(String numeroBanda) {
        this.numeroBanda = numeroBanda;
    }

    public int getCantMiembros() {
        return cantMiembros;
    }

    public void setCantMiembros(int cantMiembros) {
        this.cantMiembros = cantMiembros;
    }

    @Override
    public String toString() {
        return "Banda ["
                + "Número: " + numeroBanda
                + ", Miembros: " + cantMiembros
                + "]";
    }
}