package prog2.policia_backend.exceptions;

public class FechaPasadaException extends RuntimeException {

    public FechaPasadaException() {

        super("La fecha a guardar no puede ser pasada.");
    }
}
