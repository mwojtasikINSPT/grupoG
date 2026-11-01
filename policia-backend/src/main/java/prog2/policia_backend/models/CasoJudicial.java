package prog2.policia_backend.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

//Relaciona Asalto, Asaltante y Juez con cada caso judicial: Asalto 1 - 1 CasoJudicial N - 1 Juez
@Entity
@Getter
@NoArgsConstructor
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"asalto_id"})})
public class CasoJudicial extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean activo = true;

    @Column(unique = true)
    private String codigo;
    private boolean sentenciado;

    @OneToOne(optional = false)
    @JoinColumn(name = "asalto_id", nullable = false, unique = true)
    private Asalto asalto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "juez_id", nullable = false)
    private Juez juez;

    @OneToMany(mappedBy = "casoJudicial")
    private List<SentenciaAsaltante> sentencias = new ArrayList<>();

    private String motivoReapertura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCasoJudicial estado = EstadoCasoJudicial.EN_TRAMITE;

    public CasoJudicial(String codigo, Asalto asalto, Juez juez) {
        this.codigo = codigo;
        this.asalto = asalto;
        this.juez = juez;
    }

    public void cambiarJuez(Juez nuevoJuez) {
        this.juez = nuevoJuez;
    }

    public void actualizarEstadoSentenciado(boolean sentenciado) {
        this.sentenciado = sentenciado;
    }

    public void asignarCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void actualizarActivo(boolean activo) {
        this.activo = activo;
    }

    public void reabrirPorApelacion(String motivo) {
        this.activo = true;
        this.estado = EstadoCasoJudicial.REABIERTO_POR_APELACION;
        this.motivoReapertura = motivo;
    }

    public void cerrarAutomaticamente() {
        if (estado == EstadoCasoJudicial.EN_TRAMITE) {
            this.activo = false;
            this.estado = EstadoCasoJudicial.CERRADO;
        }
    }

    public void actualizarEstado(EstadoCasoJudicial estado) {
        this.estado = estado;
    }
}
