package grupog.sistemapolicia.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("INVESTIGADOR")
public class UsuarioInvestigador extends Usuario {

    public UsuarioInvestigador() {
    }

    public UsuarioInvestigador(String username, String password) {
        super(username, password);
    }

    @Override
    public Rol obtenerRol() {
        return Rol.INVESTIGADOR;
    }
}
