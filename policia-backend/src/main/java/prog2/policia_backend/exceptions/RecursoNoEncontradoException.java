package prog2.policia_backend.exceptions;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Long id) {
        super(recurso + " no encontrado con ID: " + id);
    }

    public RecursoNoEncontradoException(String recurso, String codigo) {
        super(recurso + " no encontrado con código: " + codigo);
    }
}