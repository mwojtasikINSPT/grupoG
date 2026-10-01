package prog2.policia_backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Relacion: Banda 1 - N Asaltante
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Banda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int cantMiembros;
    @OneToMany(mappedBy = "banda")
    private List<Asaltante> asaltantes;
    private String codigo;

    private boolean activo = true;

}
