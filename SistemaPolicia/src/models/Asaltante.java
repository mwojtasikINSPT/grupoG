package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa a una persona detenida por cometer un asalto a una sucursal
 * bancaria.
 * <p>
 * El modelo conserva los datos basicos del asaltante, su posible pertenencia a
 * una banda y el historial de asaltos cometidos.
 * </p>
 */
public class Asaltante implements Serializable {

    /**
     * Identificador unico de version para la serializacion.
     */
    private static final long serialVersionUID = 1L;

    private String clave;
    private String nombreCompleto;
    private Banda banda;
    private List<Asalto> listaAsaltos = new ArrayList<>();

    /**
     * Constructor por defecto.
     */
    public Asaltante() {
    }

    /**
     * Construye un nuevo asaltante con sus datos basicos y su posible banda.
     *
     * @param clave Codigo identificador unico del asaltante.
     * @param nombreCompleto Nombre completo del asaltante.
     * @param banda Banda a la que pertenece; puede ser {@code null}.
     */
    public Asaltante(String clave, String nombreCompleto, Banda banda) {
        this.clave = clave;
        this.nombreCompleto = nombreCompleto;
        this.banda = banda;
    }

    /**
     * Obtiene la clave del asaltante.
     *
     * @return La clave identificadora.
     */
    public String getClave() {
        return clave;
    }

    /**
     * Establece la clave del asaltante.
     *
     * @param clave La clave identificadora.
     */
    public void setClave(String clave) {
        this.clave = clave;
    }

    /**
     * Obtiene el nombre del asaltante.
     *
     * @return El nombre completo.
     */
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    /**
     * Establece el nombre del asaltante.
     *
     * @param nombreCompleto El nombre completo a asignar.
     */
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    /**
     * Obtiene la banda a la que pertenece el asaltante.
     *
     * @return La banda asociada, o {@code null} si no pertenece a ninguna.
     */
    public Banda getBanda() {
        return banda;
    }

    /**
     * Establece la banda a la que pertenece el asaltante.
     *
     * @param banda La banda a asignar.
     */
    public void setBanda(Banda banda) {
        this.banda = banda;
    }

    /**
     * Retorna una representacion en cadena de texto del asaltante.
     *
     * @return Una cadena con los datos del asaltante.
     */
    @Override
    public String toString() {
        String infoBanda = (banda != null) ? String.valueOf(banda.getNumeroBanda()) : "Ninguna (actua solo)";
        return "Asaltante ["
                + "Clave: " + clave
                + ", Nombre: " + nombreCompleto
                + ", Banda: " + infoBanda
                + "]";
    }

    /**
     * Registra un nuevo asalto en el historial del asaltante.
     *
     * @param asalto El objeto Asalto a anadir al historial.
     */
    public void agregarAsalto(Asalto asalto) {
        if (asalto != null) {
            this.listaAsaltos.add(asalto);
        }
    }

    /**
     * Obtiene la lista completa de asaltos cometidos por el asaltante.
     *
     * @return Una lista con los objetos Asalto.
     */
    public List<Asalto> getListaAsaltos() {
        return listaAsaltos;
    }
}
