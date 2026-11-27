package prog2.policia_backend.exceptions;

public class RolAbogadoInvalidoException extends RuntimeException {
    public RolAbogadoInvalidoException() {
        super("El defensor debe tener usuario y rol activos y habilitación para defensa");
    }
}
