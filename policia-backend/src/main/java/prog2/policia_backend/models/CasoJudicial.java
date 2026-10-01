package prog2.policia_backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//relaciona Asalto con Juez: Asalto 1 ─ 1 CasoJudicial N - 1 Juez
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasoJudicial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean activo = true;
    private String codigo;
    private boolean condenado;
    private int tiempoCarcel;
        
    @OneToOne
    private Asalto asalto;
    @ManyToOne
    private Asaltante asaltante;
    @ManyToOne
    private Juez juez;
    
}
