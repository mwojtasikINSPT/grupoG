package prog2.policia_backend.exceptions;

public class AbogadoNoEncontradoException extends RecursoNoEncontradoException {
    public AbogadoNoEncontradoException(String codigo) {
        super("Abogado defensor", codigo);
    }
}
