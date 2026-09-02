package models;

/**
 * Representa una cuenta de acceso con rol de vigilante.
 *
 * La cuenta conserva únicamente el código del vigilante asociado. Los datos
 * personales y laborales permanecen en el modelo {@link Vigilante}.
 *
 * @author GrupoG
 */
public class UsuarioVigilante extends Usuario {

    private static final long serialVersionUID = 1L;

    private String codigoVigilante;

    /**
     * Crea una cuenta de vigilante sin datos iniciales.
     */
    public UsuarioVigilante() {
        super();
    }

    /**
     * Crea una cuenta vinculada con el código de un vigilante.
     *
     * @param username nombre utilizado para iniciar sesión
     * @param password contraseña de la cuenta
     * @param codigoVigilante código del vigilante asociado
     */
    public UsuarioVigilante(
            String username,
            String password,
            String codigoVigilante) {

        super(username, password);
        this.codigoVigilante = codigoVigilante;
    }

    /**
     * Obtiene el código del vigilante vinculado con la cuenta.
     *
     * @return código del vigilante asociado
     */
    public String getCodigoVigilante() {
        return codigoVigilante;
    }

    /**
     * Modifica el código del vigilante vinculado con la cuenta.
     *
     * @param codigoVigilante nuevo código asociado
     */
    public void setCodigoVigilante(String codigoVigilante) {
        this.codigoVigilante = codigoVigilante;
    }

    /**
     * Devuelve el rol correspondiente a la cuenta.
     *
     * @return rol de vigilante
     */
    @Override
    public Rol obtenerRol() {
        return Rol.VIGILANTE;
    }
}