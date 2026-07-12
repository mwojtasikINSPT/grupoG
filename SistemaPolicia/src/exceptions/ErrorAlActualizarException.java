package exceptions;

/**
 * Excepción lanzada cuando ocurre un error durante el proceso de actualización
 * de un objeto en el sistema (por ejemplo, al intentar modificar registros en
 * el archivo).
 */
public class ErrorAlActualizarException extends Exception {

    /**
     * Construye una nueva excepción indicando el objeto afectado y la causa del
     * fallo.
     *
     * @param tipoObjeto El nombre de la clase o entidad que se intentaba
     * actualizar.
     * @param motivo Una descripción detallada de por qué falló la operación.
     */
    public ErrorAlActualizarException(String tipoObjeto, String motivo) {
        super("Error al intentar actualizar " + tipoObjeto + ". Motivo: " + motivo);
    }

}
