package prog2.policia_backend.exceptions;

public class AsaltanteNoParticipaEnAsaltoException extends RuntimeException {

    public AsaltanteNoParticipaEnAsaltoException() {
        super("El asaltante indicado no participó del asalto");
    }
}