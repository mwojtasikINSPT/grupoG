package exceptions;

public class ErrorAlActualizarException extends Exception {
    // Incluyo el motivo para saber qué falló en el guardado

    public ErrorAlActualizarException(String tipoObjeto, String motivo) {
        super("Error al intentar actualizar " + tipoObjeto + ". Motivo: " + motivo);
    }

}
