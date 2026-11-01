package prog2.policia_frontend.dtos;

/**
 * Datos requeridos para que el usuario conectado cambie su contraseña.
 */
public class CambioPasswordDTO {

    private String passwordActual;
    private String passwordNueva;

    public CambioPasswordDTO() {
    }

    public CambioPasswordDTO(String passwordActual, String passwordNueva) {
        this.passwordActual = passwordActual;
        this.passwordNueva = passwordNueva;
    }

    public String getPasswordActual() {
        return passwordActual;
    }

    public void setPasswordActual(String passwordActual) {
        this.passwordActual = passwordActual;
    }

    public String getPasswordNueva() {
        return passwordNueva;
    }

    public void setPasswordNueva(String passwordNueva) {
        this.passwordNueva = passwordNueva;
    }
}
