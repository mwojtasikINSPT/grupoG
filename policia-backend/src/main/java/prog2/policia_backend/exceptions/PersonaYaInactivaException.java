package prog2.policia_backend.exceptions;

public class PersonaYaInactivaException extends RuntimeException {
    public PersonaYaInactivaException() {
        super("La persona ya está dada de baja");
    }
}
