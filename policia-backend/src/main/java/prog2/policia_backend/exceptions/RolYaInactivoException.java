package prog2.policia_backend.exceptions;

public class RolYaInactivoException extends RuntimeException {

    public RolYaInactivoException(){
        super("El rol ya se encuentra inactivo");
    }
}
