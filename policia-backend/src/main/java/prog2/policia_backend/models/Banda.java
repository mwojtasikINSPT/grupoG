package prog2.policia_backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

//Relacion: Banda 1 - N Asaltante
@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Banda extends EntidadAuditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Formula("(SELECT COUNT(*) FROM asaltante a WHERE a.banda_id = id AND a.activo = true)")
    private int cantMiembros;

    @OneToMany(mappedBy = "banda")
    private List<Asaltante> asaltantes;
    private String codigo;

    private boolean activo = true;

}
