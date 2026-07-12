package exceptions;

/**
 * Excepción lanzada cuando ocurre un error durante el proceso de persistencia o
 * guardado de un objeto en el sistema.
 */
public class ErrorAlGuardarException extends Exception {

    /**
     * Construye una nueva excepción indicando el objeto que se intentaba
     * guardar y la razón específica del fallo.
     *
     * @param tipoObjeto El nombre de la entidad o clase que no pudo ser
     * guardada.
     * @param motivo La descripción de la causa del error (ej. error de I/O,
     * formato inválido).
     */
    public ErrorAlGuardarException(String tipoObjeto, String motivo) {
        super("Error al intentar guardar " + tipoObjeto + ". Motivo: " + motivo);
    }
}
