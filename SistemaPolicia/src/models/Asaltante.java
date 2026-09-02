package models;

import java.io.Serializable;

/**
 * Representa a una persona identificada como asaltante.
 * Los asaltos realizados se registran por separado en el modelo {@link Asalto}.
 *
 * @author Grupo G
 */
public class Asaltante implements Serializable {

    private static final long serialVersionUID = 1L;

    private String clave;
    private String nombreCompleto;
    private String idBanda;

    /**
     * Crea un asaltante vacío.
     */
    public Asaltante() {
    }

    /**
     * Crea un asaltante utilizando el identificador de su banda.
     *
     * @param clave identificador del asaltante
     * @param nombreCompleto nombre completo del asaltante
     * @param idBanda identificador de la banda; puede ser {@code null}
     */
    public Asaltante(
            String clave,
            String nombreCompleto,
            String idBanda) {

        this.clave = clave;
        this.nombreCompleto = nombreCompleto;
        this.idBanda = idBanda;
    }

   

    /**
     * Obtiene la clave del asaltante.
     *
     * @return clave del asaltante
     */
    public String getClave() {
        return clave;
    }

    /**
     * Asigna la clave del asaltante.
     *
     * @param clave nueva clave
     */
    public void setClave(String clave) {
        this.clave = clave;
    }

    /**
     * Obtiene el nombre completo.
     *
     * @return nombre completo del asaltante
     */
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    /**
     * Asigna el nombre completo.
     *
     * @param nombreCompleto nuevo nombre completo
     */
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    /**
     * Obtiene el identificador de la banda.
     *
     * @return identificador de la banda, o {@code null} si actúa solo
     */
    public String getIdBanda() {
        return idBanda;
    }

    /**
     * Asigna el identificador de la banda.
     *
     * @param idBanda identificador de la banda; puede ser {@code null}
     */
    public void setIdBanda(String idBanda) {
        this.idBanda = idBanda;
    }

    /**
     * Devuelve los datos principales del asaltante.
     *
     * @return representación textual del asaltante
     */
    @Override
    public String toString() {
        String bandaMostrada =
                idBanda != null ? idBanda : "Ninguna (actúa solo)";

        return "Asaltante ["
                + "Clave: " + clave
                + ", Nombre: " + nombreCompleto
                + ", Banda N°: " + bandaMostrada
                + "]";
    }
}