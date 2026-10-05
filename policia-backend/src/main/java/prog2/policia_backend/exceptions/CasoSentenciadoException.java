package prog2.policia_backend.exceptions;

public class CasoSentenciadoException extends RuntimeException {

    public CasoSentenciadoException() {
        super("El caso judicial ya se encuentra sentenciado y fue cerrado. Modifique estado para cambiar sentencia");
    }
}