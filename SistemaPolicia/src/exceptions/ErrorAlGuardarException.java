package exceptions;

public class ErrorAlGuardarException extends Exception {

    // Incluyo el motivo para saber qué falló en el guardado
    public ErrorAlGuardarException(String tipoObjeto, String motivo) {
        super("Error al intentar guardar " + tipoObjeto + ". Motivo: " + motivo);
    }
}