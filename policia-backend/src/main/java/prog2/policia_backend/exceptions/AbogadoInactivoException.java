package prog2.policia_backend.exceptions;

public class AbogadoInactivoException extends RuntimeException {
    public AbogadoInactivoException() {
        super("El abogado defensor está inactivo");
    }
}
