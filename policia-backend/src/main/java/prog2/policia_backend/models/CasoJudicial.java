package prog2.policia_backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

//Relaciona Asalto, Asaltante y Juez con cada caso judicial: Asalto 1 ─ N CasoJudicial N - 1 Juez
@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"asalto_id", "asaltante_id"})
})
public class CasoJudicial extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean activo = true;
    private String codigo;
    private boolean condenado;
    private int tiempoCarcel;
    private boolean sentenciado;

    @ManyToOne
    private Asalto asalto;

    @ManyToMany
    private List<Asaltante> asaltantes = new ArrayList<>();

    @ManyToMany
    private List<Juez> jueces = new ArrayList<>();

}
