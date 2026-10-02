package prog2.policia_backend.exceptions;

public class ContratoVigilanciaDuplicadoException
        extends RuntimeException {

    public ContratoVigilanciaDuplicadoException() {
        super("El vigilante ya tiene un contrato activo en esa fecha");
    }
}