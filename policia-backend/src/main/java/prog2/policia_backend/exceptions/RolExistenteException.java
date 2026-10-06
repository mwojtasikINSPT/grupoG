package prog2.policia_backend.exceptions;

public class RolExistenteException extends RuntimeException {

    public RolExistenteException(){
    super("Ya existe ese Rol");
}
}
