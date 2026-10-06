package prog2.policia_backend.exceptions;

public class AsaltanteConCasoJudicialException extends RuntimeException {

    public AsaltanteConCasoJudicialException() {
        super("No se puede eliminar al asaltante, tiene Caso Judicial iniciado. Gestionar antes de eliminar");

    }

}
