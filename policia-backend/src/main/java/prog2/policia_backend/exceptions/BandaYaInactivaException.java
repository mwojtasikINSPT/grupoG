package prog2.policia_backend.exceptions;

public class BandaYaInactivaException extends RuntimeException {

    public BandaYaInactivaException() {
        super("La banda ya se encuentra inactiva.");
    }
}
