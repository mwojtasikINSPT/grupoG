package prog2.policia_backend.exceptions;

public class CasoCondenadoSinCarcelException extends RuntimeException {

    public CasoCondenadoSinCarcelException() {
        super("Un caso condenado debe tener un tiempo de cárcel mayor a cero");
    }
}
