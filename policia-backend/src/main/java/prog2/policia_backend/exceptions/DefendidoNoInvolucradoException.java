package prog2.policia_backend.exceptions;

public class DefendidoNoInvolucradoException extends RuntimeException {
    public DefendidoNoInvolucradoException() {
        super("El defendido no esta involucrado en el caso judicial consultado");
    }
}
