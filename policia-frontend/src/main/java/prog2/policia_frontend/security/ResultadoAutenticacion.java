package prog2.policia_frontend.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import prog2.policia_frontend.dtos.UsuarioDTO;

/**
 * Agrupa la identidad confirmada por el backend y los permisos de su rol.
 */
public class ResultadoAutenticacion {

    private final UsuarioDTO usuario;
    private final Set<String> permisos;

    public ResultadoAutenticacion(
            UsuarioDTO usuario,
            Set<String> permisos) {

        this.usuario = usuario;
        this.permisos = permisos == null
                ? new HashSet<>()
                : new HashSet<>(permisos);
    }

    public UsuarioDTO getUsuario() {
        return usuario;
    }

    public Set<String> getPermisos() {
        return Collections.unmodifiableSet(permisos);
    }
}
