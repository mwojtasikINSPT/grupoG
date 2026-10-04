package prog2.policia_backend.exceptions;

public class BandaInactivaException extends RuntimeException {

    public BandaInactivaException() {
        super("No se puede asignar, la banda está dada de baja");
    }
}