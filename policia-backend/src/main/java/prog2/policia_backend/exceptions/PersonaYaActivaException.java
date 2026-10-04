package prog2.policia_backend.exceptions;

public class PersonaYaActivaException extends RuntimeException {

    public PersonaYaActivaException() {
        super("La persona ya está activa");
    }
}