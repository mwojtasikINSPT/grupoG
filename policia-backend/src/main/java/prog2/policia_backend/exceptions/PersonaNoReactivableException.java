package prog2.policia_backend.exceptions;

public class PersonaNoReactivableException extends RuntimeException {

    public PersonaNoReactivableException() {
        super("La persona no puede ser reactivada porque su baja es definitiva");
    }
}
