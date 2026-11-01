package prog2.policia_backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

//Clase para registrar el resultado individual de un asaltante dentro de un caso
@Entity
@Getter
@NoArgsConstructor
@Table(
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"caso_judicial_id", "asaltante_id"})
        }
)
public class SentenciaAsaltante extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "caso_judicial_id", nullable = false)
    private CasoJudicial casoJudicial;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asaltante_id", nullable = false)
    private Asaltante asaltante;

    private boolean sentenciado;
    private boolean condenado;
    private int tiempoCarcel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoParticipacionJudicial estado = EstadoParticipacionJudicial.PENDIENTE;

    public SentenciaAsaltante(CasoJudicial casoJudicial, Asaltante asaltante) {
        this.casoJudicial = casoJudicial;
        this.asaltante = asaltante;
    }

    public void registrarSentencia(boolean condenado, int tiempoCarcel) {
        this.condenado = condenado;
        this.tiempoCarcel = tiempoCarcel;
        this.sentenciado = true;
        this.estado = EstadoParticipacionJudicial.SENTENCIADO;
    }

    public void extinguirPorFallecimiento() {
        if (estado == EstadoParticipacionJudicial.PENDIENTE) {
            this.estado = EstadoParticipacionJudicial.EXTINGUIDO_POR_FALLECIMIENTO;
        }
    }

    public void modificarSentencia(boolean condenado, int tiempoCarcel) {
        this.condenado = condenado;
        this.tiempoCarcel = tiempoCarcel;
    }
}
