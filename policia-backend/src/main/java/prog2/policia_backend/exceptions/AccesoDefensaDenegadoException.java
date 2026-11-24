package prog2.policia_backend.exceptions;

import org.springframework.security.access.AccessDeniedException;

public class AccesoDefensaDenegadoException extends AccessDeniedException {
    public AccesoDefensaDenegadoException(String mensaje) {
        super(mensaje);
    }
}
