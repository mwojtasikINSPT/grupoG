package prog2.policia_backend.models;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/** Registro historico: la reasignacion del defendido no cambia el autor. */
@Entity
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = @Index(name = "idx_prueba_defendido_caso", columnList = "asaltante_id,caso_judicial_id"))
public class PruebaDefensa extends EntidadAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4000, updatable = false)
    private String descripcion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaPresentacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "abogado_id", nullable = false, updatable = false)
    private Usuario abogado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asaltante_id", nullable = false, updatable = false)
    private Asaltante asaltante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caso_judicial_id", nullable = false, updatable = false)
    private CasoJudicial casoJudicial;

    public PruebaDefensa(String descripcion, Usuario abogado, Asaltante asaltante, CasoJudicial casoJudicial) {
        this.descripcion = descripcion;
        this.fechaPresentacion = LocalDateTime.now();
        this.abogado = abogado;
        this.asaltante = asaltante;
        this.casoJudicial = casoJudicial;
    }
}
