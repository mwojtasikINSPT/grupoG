package prog2.policia_backend.exceptions;

public class EdadMaximaExcedidaException extends RuntimeException {

    public EdadMaximaExcedidaException() {
        super("La edad no puede superar los 65 años");
    }
}
