package prog2.policia_backend.exceptions;

public class SucursalNoReactivableException extends RuntimeException {

    public SucursalNoReactivableException() {
        super("La sucursal tiene cierre definitivo y no puede reabrirse");
    }
}
