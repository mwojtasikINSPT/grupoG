package prog2.policia_backend.utils;

import org.springframework.security.core.Authentication;

public final class SeguridadUtils {

    private SeguridadUtils() {
    }

    public static boolean tieneLecturaCompleta(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("LECTURA"));
    }
}
