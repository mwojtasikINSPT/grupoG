package prog2.policia_backend.exceptions;

public class CasoCerradoParaPruebaException extends RuntimeException {
    public CasoCerradoParaPruebaException() {
        super("Solo se pueden presentar pruebas de defensa en casos judiciales abiertos");
    }
}
