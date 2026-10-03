package prog2.policia_backend.exceptions;

public class MotivoCierreSucursalObligatorioException extends RuntimeException {

    public MotivoCierreSucursalObligatorioException() {
        super("El motivo de cierre es obligatorio");
    }
}