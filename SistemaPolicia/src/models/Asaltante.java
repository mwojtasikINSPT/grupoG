package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa a una persona detenida por cometer un asalto a una sucursal
 * bancaria. Relacion N-M con Sucursales (a través de Asalto). Relación N-1 con
 * Banda.
 */
public class Asaltante implements Serializable {

    /**
     * Identificador único de versión para la serialización.
     */
    private static final long serialVersionUID = 1L;

    private String clave;
    private String nombreCompleto;
    private Banda banda;

    /**
     * Constructor por defecto
     */
    public Asaltante() {
        this.listaAsaltos = new ArrayList<>();
    }

    /**
     * Construye un nuevo asaltante con sus datos básicos y pertenencia a banda.
     *
     * @param clave Código identificador único del asaltante.
     * @param nombreCompleto Nombre completo del asaltante.
     * @param banda La banda a la que pertenece el asaltante.
     */
    public Asaltante(String clave, String nombreCompleto, Banda banda) {
        this.clave = clave;
        this.nombreCompleto = nombreCompleto;
        this.banda = banda;
        this(); // Llamo al constructor vacío, que ya inicializa la lista;
    }

    // Getters y Setters
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
     * @param clave la clave identificadora.
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
     * @return La instancia de Banda asociada.
     */
    public Banda getBanda() {
        return banda;
    }

    /**
     * Establece la banda a la que pertenece el asaltante.
     *
     * @param banda La instancia de Banda a asignar.
     */
    public void setBanda(Banda banda) {
        this.banda = banda;
    }

    /**
     * Retorna una representación en cadena de texto del asaltante.
     *
     * @return Una cadena con los datos del asaltante.
     */
    @Override
    public String toString() {
        // Valido si tiene banda o es null para evitar errores al imprimir
        String infoBanda = (banda != null) ? String.valueOf(banda.getNumeroBanda()) : "Ninguna (Actúa solo)";
        return "Asaltante ["
                + "Clave: " + clave
                + ", Nombre: " + nombreCompleto
                + ", Banda N°: " + infoBanda
                + "]";
    }

    private List<Asalto> listaAsaltos;

    /**
     * Registra un nuevo asalto en el historial del asaltante.
     *
     * @param asalto El objeto Asalto a añadir al historial.
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
