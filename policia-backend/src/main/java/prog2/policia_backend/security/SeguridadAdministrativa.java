package prog2.policia_backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("seguridadAdministrativa")
public class SeguridadAdministrativa {
    public boolean esAdministrador(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UsuarioDetails)) {
            return false;
        }
        UsuarioDetails usuario = (UsuarioDetails) authentication.getPrincipal();
        return "ADMINISTRADOR".equalsIgnoreCase(usuario.getNombreRol());
    }
}
