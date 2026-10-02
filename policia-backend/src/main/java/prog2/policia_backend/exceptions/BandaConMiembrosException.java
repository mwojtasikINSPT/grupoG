package prog2.policia_backend.exceptions;

public class BandaConMiembrosException extends RuntimeException {

    public BandaConMiembrosException() {
        super("No se puede eliminar la banda porque tiene miembros activos");
    }
}
