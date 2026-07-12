package exceptions;

/**
 * Excepción lanzada cuando ocurre un error durante el proceso de lectura o
 * recuperación de datos desde el sistema de almacenamiento.
 */
public class ErrorAlLeerException extends Exception {

    /**
     * Construye una nueva excepción indicando el objeto que se intentaba leer y
     * la razón específica del fallo.
     *
     * @param tipoObjeto El nombre de la entidad o clase que se intentaba
     * recuperar.
     * @param motivo La descripción de la causa del error (ej. archivo no
     * encontrado, error de formato).
     */
    public ErrorAlLeerException(String tipoObjeto, String motivo) {
        super("Error al intentar leer " + tipoObjeto + ". Motivo: " + motivo);
    }
}
