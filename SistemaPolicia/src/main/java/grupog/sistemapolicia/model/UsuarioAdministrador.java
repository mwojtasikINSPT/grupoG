package grupog.sistemapolicia.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMINISTRADOR")
public class UsuarioAdministrador extends Usuario {

    public UsuarioAdministrador() {
    }

    public UsuarioAdministrador(String username, String password) {
        super(username, password);
    }

    @Override
    public Rol obtenerRol() {
        return Rol.ADMINISTRADOR;
    }
}
