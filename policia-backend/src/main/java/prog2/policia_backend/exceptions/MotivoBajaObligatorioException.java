package prog2.policia_backend.exceptions;

public class MotivoBajaObligatorioException extends RuntimeException {

    public MotivoBajaObligatorioException() {
        super("El motivo de baja es obligatorio");
    }
}
