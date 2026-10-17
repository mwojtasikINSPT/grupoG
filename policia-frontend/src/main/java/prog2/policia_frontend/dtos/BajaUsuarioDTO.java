package prog2.policia_frontend.dtos;

/*
 * Datos enviados al backend cuando
 * se solicita la baja de un usuario.
 */
public class BajaUsuarioDTO {

    private String motivoBaja;

    /*
     * Constructor vacío necesario
     * para convertir objetos JSON.
     */
    public BajaUsuarioDTO() {
    }

    /*
     * Permite crear rápidamente el objeto
     * indicando el motivo seleccionado.
     */
    public BajaUsuarioDTO(
            String motivoBaja) {

        this.motivoBaja = motivoBaja;
    }

    public String getMotivoBaja() {
        return motivoBaja;
    }

    public void setMotivoBaja(
            String motivoBaja) {

        this.motivoBaja = motivoBaja;
    }
}