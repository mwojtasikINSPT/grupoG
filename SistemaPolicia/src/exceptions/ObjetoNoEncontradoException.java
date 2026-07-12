package exceptions;

/**
 * Excepción lanzada cuando una operación de búsqueda no encuentra un registro
 * específico en la base de datos o el sistema de almacenamiento.
 */
public class ObjetoNoEncontradoException extends Exception {

    /**
     * Construye una nueva excepción especificando el tipo de entidad y el
     * identificador que no pudo ser localizado.
     *
     * @param tipoObjeto El nombre de la entidad o clase que se intentaba
     * buscar.
     * @param id El identificador único o clave utilizada para la búsqueda.
     */
    public ObjetoNoEncontradoException(String tipoObjeto, String id) {
        super("Error: No se encontró el registro de " + tipoObjeto + " con el identificador: " + id);
    }
}
