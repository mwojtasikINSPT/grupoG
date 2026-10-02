package prog2.policia_backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

// Un Asalto puede tener varios Asaltantes y tiene una Sucursal
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asalto {

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
}
