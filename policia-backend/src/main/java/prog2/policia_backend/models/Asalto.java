package prog2.policia_backend.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import prog2.policia_backend.exceptions.AsaltanteDuplicadoException;
import prog2.policia_backend.exceptions.OperacionNoPermitidaException;
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
    private List<Asaltante> asaltantes = new ArrayList<>();

    @ManyToOne
    private Sucursal sucursal;

    @OneToOne(mappedBy = "asalto")
    private CasoJudicial casoJudicial;

    public Asalto(LocalDate fecha, Sucursal sucursal, List<Asaltante> asaltantes) {
        this.fecha = fecha;
        this.sucursal = sucursal;
        if (asaltantes == null) {
            throw new OperacionNoPermitidaException("La lista de asaltantes es obligatoria.");
        }
        for (Asaltante asaltante : asaltantes) {
            agregarAsaltante(asaltante);
        }
    }

    public List<Asaltante> getAsaltantes() {
        return Collections.unmodifiableList(asaltantes);
    }

    public void asignarCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void cambiarSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public void agregarAsaltante(Asaltante asaltante) {
        if (asaltante == null) {
            throw new OperacionNoPermitidaException("El asaltante es obligatorio.");
        }
        for (Asaltante existente : asaltantes) {
            if (esMismoAsaltante(existente, asaltante)) {
                throw new AsaltanteDuplicadoException();
            }
        }
        this.asaltantes.add(asaltante);
    }

    public void quitarAsaltante(Asaltante asaltante) {
        Iterator<Asaltante> iterator = asaltantes.iterator();
        while (iterator.hasNext()) {
            if (esMismoAsaltante(iterator.next(), asaltante)) {
                iterator.remove();
                return;
            }
        }
    }

    private boolean esMismoAsaltante(Asaltante primero, Asaltante segundo) {
        return primero == segundo || (segundo != null && primero.getId() != null
                && primero.getId().equals(segundo.getId()));
    }
}
