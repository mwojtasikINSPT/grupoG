package prog2.policia_backend.exceptions;

public class EdadInvalidaException extends RuntimeException {

    public EdadInvalidaException() {
        super("La edad no puede ser inferior a 18 años ni superar los 65");
    }
}
