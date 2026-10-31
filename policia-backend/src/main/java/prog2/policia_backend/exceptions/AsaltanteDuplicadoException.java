package prog2.policia_backend.exceptions;

public class AsaltanteDuplicadoException extends RuntimeException {

    public AsaltanteDuplicadoException() {
        super("No se puede agregar un asaltante duplicado al mismo asalto.");
    }
}