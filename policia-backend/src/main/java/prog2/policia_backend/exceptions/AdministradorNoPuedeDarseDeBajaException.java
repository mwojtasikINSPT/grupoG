package prog2.policia_backend.exceptions;

public class AdministradorNoPuedeDarseDeBajaException extends RuntimeException {

    public AdministradorNoPuedeDarseDeBajaException(){
        super("Un administrador no puede darse de baja a sí mismo");
    }
}
