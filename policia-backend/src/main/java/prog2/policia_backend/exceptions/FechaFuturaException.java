package prog2.policia_backend.exceptions;

public class FechaFuturaException extends RuntimeException {

    public FechaFuturaException() {

        super("La fecha a guardar no puede ser futura.");
    }

}
