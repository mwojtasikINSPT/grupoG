package prog2.policia_backend.exceptions;

public class CasoJudicialActivoException extends RuntimeException {

    public CasoJudicialActivoException() {
        super("El caso judicial ya se encuentra activo.");
    }
}
