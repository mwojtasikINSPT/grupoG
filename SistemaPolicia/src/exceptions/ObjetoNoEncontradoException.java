package exceptions;

public class ObjetoNoEncontradoException extends Exception {

    // Uso este constructor para detallar qué objeto falló y con qué ID
    public ObjetoNoEncontradoException(String tipoObjeto, String id) {
        super("Error: No se encontró el registro de " + tipoObjeto + " con el identificador: " + id);
    }
}