package prog2.policia_backend.exceptions;

public class BandaYaActivaException extends RuntimeException {

    public BandaYaActivaException() {
        super("La banda ya se encuentra activa.");
    }
}
