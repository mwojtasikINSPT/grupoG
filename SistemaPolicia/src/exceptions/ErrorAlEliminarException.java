package exceptions;

public class ErrorAlEliminarException extends Exception {

    public ErrorAlEliminarException(String tipoObjeto, String motivo) {
        super("Error al intentar eliminar " + tipoObjeto + ". Motivo: " + motivo);
    }
}
