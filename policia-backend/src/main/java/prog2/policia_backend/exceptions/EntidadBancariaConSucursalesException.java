package prog2.policia_backend.exceptions;

public class EntidadBancariaConSucursalesException extends RuntimeException {

    public EntidadBancariaConSucursalesException() {
        super("No se puede dar de baja la entidad bancaria porque tiene sucursales activas");
    }
}