package prog2.policia_backend.exceptions;

public class EntidadInactivaException extends RuntimeException {

    public EntidadInactivaException() {
        super("La entidad se encuentra inactiva.");
    }
}
