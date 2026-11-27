package prog2.policia_backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("seguridadAdministrativa")
public class SeguridadAdministrativa {
    public boolean puedeAsignarAbogados(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        for (GrantedAuthority autoridad : authentication.getAuthorities()) {
            if ("ESCRITURA_ASALTANTE".equals(autoridad.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
