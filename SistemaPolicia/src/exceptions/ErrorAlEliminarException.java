package exceptions;

/**
 * Excepción lanzada cuando ocurre un error durante el proceso de eliminación de
 * un objeto en el sistema (por ejemplo, al intentar borrar un registro del
 * archivo).
 */
public class ErrorAlEliminarException extends Exception {

    /**
     * Construye una nueva excepción indicando el objeto afectado y la causa del
     * fallo.
     *
     * @param tipoObjeto El nombre de la clase o entidad que se intentaba
     * eliminar.
     * @param motivo Una descripción detallada de la causa que impidió la
     * eliminación.
     */
    public ErrorAlEliminarException(String tipoObjeto, String motivo) {
        super("Error al intentar eliminar " + tipoObjeto + ". Motivo: " + motivo);
    }
}
