package exceptions;


public class ErrorAlLeerException extends Exception {
   // Incluyo el motivo para saber qué falló en el guardado
    public ErrorAlLeerException(String tipoObjeto, String motivo) {
        super("Error al intentar leer " + tipoObjeto + ". Motivo: " + motivo);
    }
}
