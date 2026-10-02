package prog2.policia_backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Relaciona Asalto, Asaltante y Juez con cada caso judicial: Asalto 1 ─ N CasoJudicial N - 1 Juez
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"asalto_id", "asaltante_id"})
})
public class CasoJudicial {

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

    @ManyToOne
    private Asaltante asaltante;

    @ManyToOne
    private Juez juez;

}
