package prog2.policia_backend.exceptions;

public class RolAbogadoInvalidoException extends RuntimeException {
    public RolAbogadoInvalidoException() {
        super("El usuario debe tener el rol activo ABOGADO_DEFENSOR");
    }
}
