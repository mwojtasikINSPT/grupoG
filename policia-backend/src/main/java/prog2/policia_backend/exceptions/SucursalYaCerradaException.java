package prog2.policia_backend.exceptions;

public class SucursalYaCerradaException extends RuntimeException {

    public SucursalYaCerradaException() {
        super("La sucursal ya está cerrada");
    }
}