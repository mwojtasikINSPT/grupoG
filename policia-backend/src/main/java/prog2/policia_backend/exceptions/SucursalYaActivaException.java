package prog2.policia_backend.exceptions;

public class SucursalYaActivaException extends RuntimeException {

    public SucursalYaActivaException() {
        super("La sucursal ya está activa");
    }
}