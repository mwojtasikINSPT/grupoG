package prog2.policia_backend.exceptions;

public class MotivoBajaAsaltanteInvalidoException extends RuntimeException {

    public MotivoBajaAsaltanteInvalidoException() {
        super("Un asaltante solo puede ser dado de baja por fallecimiento");
    }
}