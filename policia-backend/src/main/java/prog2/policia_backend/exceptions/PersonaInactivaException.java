package prog2.policia_backend.exceptions;

public class PersonaInactivaException extends RuntimeException {

    public PersonaInactivaException() {
        super("No se puede modificar, la persona está dada de baja");
    }
}