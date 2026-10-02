package prog2.policia_backend.exceptions;

public class ContratoVigilanciaCumplidoException extends RuntimeException {

    public ContratoVigilanciaCumplidoException() {
        super("No se puede eliminar un contrato de vigilancia ya iniciado o cumplido");
    }
}