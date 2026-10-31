package prog2.policia_backend.exceptions;

public class DatosCondenaInvalidosException extends RuntimeException {

    public DatosCondenaInvalidosException() {
        super("El tiempo de cárcel no puede ser negativo.");
    }
}