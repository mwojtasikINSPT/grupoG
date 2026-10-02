package prog2.policia_backend.exceptions;

public class CasoSinCondenaConCarcelException extends RuntimeException {

    public CasoSinCondenaConCarcelException() {
        super("Un caso sin condena no puede tener tiempo de cárcel");
    }
}
