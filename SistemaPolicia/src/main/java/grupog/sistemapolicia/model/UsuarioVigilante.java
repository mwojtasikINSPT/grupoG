package grupog.sistemapolicia.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
@DiscriminatorValue("VIGILANTE")
public class UsuarioVigilante extends Usuario {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vigilante_codigo", unique = true)
    private Vigilante vigilante;

    public UsuarioVigilante() {
    }

    public UsuarioVigilante(String username, String password,
            Vigilante vigilante) {
        super(username, password);
        this.vigilante = vigilante;
    }

    public Vigilante getVigilante() {
        return vigilante;
    }

    public void setVigilante(Vigilante vigilante) {
        this.vigilante = vigilante;
    }

    @Override
    public Rol obtenerRol() {
        return Rol.VIGILANTE;
    }
}
