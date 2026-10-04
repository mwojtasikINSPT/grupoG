package prog2.policia_backend.exceptions;

public class EntidadYaInactivaException extends RuntimeException {

    public EntidadYaInactivaException() {
        super("La entidad ya está inactiva");
    }
}