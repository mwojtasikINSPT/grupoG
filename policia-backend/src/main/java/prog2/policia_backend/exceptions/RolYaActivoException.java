package prog2.policia_backend.exceptions;

public class RolYaActivoException extends RuntimeException {

    public RolYaActivoException(){
        super("El rol ya se encuentra activo");
    }
}
