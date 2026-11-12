package prog2.policia_backend.exceptions;

public class EdadInvalidaException extends RuntimeException {

    public EdadInvalidaException() {
        super("Revise la edad ingresada. Debe ser mayor de edad y menor a 70");
    }
}
