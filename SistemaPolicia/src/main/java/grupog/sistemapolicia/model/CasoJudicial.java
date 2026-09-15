package grupog.sistemapolicia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "casos_judiciales")
public class CasoJudicial {

    @Id
    @Column(name = "asalto_id", length = 20)
    private String idAsalto;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "asalto_id")
    private Asalto asalto;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "juez_clave", nullable = false)
    private Juez juez;

    @Column(name = "condenado", nullable = false)
    private boolean condenado;

    @Column(name = "meses_carcel", nullable = false)
    private int mesesCarcel;

    public CasoJudicial() {
    }

    public CasoJudicial(Asalto asalto, Juez juez,
            boolean condenado, int mesesCarcel) {
        this.asalto = asalto;
        this.idAsalto = asalto != null ? asalto.getIdAsalto() : null;
        this.juez = juez;
        this.condenado = condenado;
        this.mesesCarcel = condenado ? mesesCarcel : 0;
    }

    public String getIdAsalto() {
        return idAsalto;
    }

    public Asalto getAsalto() {
        return asalto;
    }

    public void setAsalto(Asalto asalto) {
        this.asalto = asalto;
        this.idAsalto = asalto != null ? asalto.getIdAsalto() : null;
    }

    public Juez getJuez() {
        return juez;
    }

    public void setJuez(Juez juez) {
        this.juez = juez;
    }

    public boolean isCondenado() {
        return condenado;
    }

    public void setCondenado(boolean condenado) {
        this.condenado = condenado;
        if (!condenado) {
            this.mesesCarcel = 0;
        }
    }

    public int getMesesCarcel() {
        return mesesCarcel;
    }

    public void setMesesCarcel(int mesesCarcel) {
        this.mesesCarcel = condenado ? Math.max(0, mesesCarcel) : 0;
    }
}
