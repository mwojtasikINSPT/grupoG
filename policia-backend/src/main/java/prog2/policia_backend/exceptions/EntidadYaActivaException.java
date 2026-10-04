package prog2.policia_backend.exceptions;

public class EntidadYaActivaException extends RuntimeException {

    public EntidadYaActivaException() {
        super("La entidad bancaria ya está activa");
    }
}