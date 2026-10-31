package prog2.policia_backend.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Un Asalto puede tener varios Asaltantes y tiene una Sucursal
@Entity
@Getter
@NoArgsConstructor
public class Asalto extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    private String codigo;

    @ManyToMany
    @JoinTable(
            name = "asalto_asaltante",
            joinColumns = @JoinColumn(name = "asalto_id"),
            inverseJoinColumns = @JoinColumn(name = "asaltante_id")
    )
    private List<Asaltante> asaltantes;

    @ManyToOne
    private Sucursal sucursal;

    @OneToOne(mappedBy = "asalto")
    private CasoJudicial casoJudicial;

    public Asalto(LocalDate fecha, Sucursal sucursal, List<Asaltante> asaltantes) {
        this.fecha = fecha;
        this.sucursal = sucursal;
        this.asaltantes = new ArrayList<>(asaltantes);
    }

    public void asignarCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void cambiarSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public void agregarAsaltante(Asaltante asaltante) {
        this.asaltantes.add(asaltante);
    }

    public void quitarAsaltante(Asaltante asaltante) {
        this.asaltantes.remove(asaltante);
    }
}
