package models;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Representa el evento de un asalto a una sucursal bancaria por parte de un
 * asaltante en una fecha determinada. Resuelve la relación N:M entre Asaltante
 * y Sucursal.
 */
public class Asalto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String idAsalto;
    private Asaltante asaltante;
    private Sucursal sucursal;
    private LocalDate fecha;

    /**
     * Constructor por defecto.
     */
    public Asalto() {
    }

    /**
     * Construye una instancia de Asalto con todos sus atributos.
     *
     * @param idAsalto Identificador único del asalto.
     * @param asaltante El asaltante responsable del hecho.
     * @param sucursal La sucursal donde ocurrió el evento.
     * @param fecha La fecha en la que ocurrió el asalto.
     */
    public Asalto(String idAsalto, Asaltante asaltante, Sucursal sucursal, LocalDate fecha) {
        this.idAsalto = idAsalto;
        this.asaltante = asaltante;
        this.sucursal = sucursal;
        this.fecha = fecha;
    }

    /**
     * Obtiene el identificador del asalto.
     *
     * @return El ID del asalto.
     */
    public String getIdAsalto() {
        return idAsalto;
    }

    /**
     * Establece el identificador del asalto.
     *
     * @param idAsalto El ID a asignar.
     */
    public void setIdAsalto(String idAsalto) {
        this.idAsalto = idAsalto;
    }

    /**
     * Obtiene el asaltante involucrado.
     *
     * @return El objeto Asaltante.
     */
    public Asaltante getAsaltante() {
        return asaltante;
    }

    /**
     * Establece el asaltante involucrado.
     *
     * @param asaltante El asaltante a asignar.
     */
    public void setAsaltante(Asaltante asaltante) {
        this.asaltante = asaltante;
    }

    /**
     * Obtiene la sucursal afectada.
     *
     * @return El objeto Sucursal.
     */
    public Sucursal getSucursal() {
        return sucursal;
    }

    /**
     * Establece la sucursal afectada.
     *
     * @param sucursal La sucursal a asignar.
     */
    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    /**
     * Obtiene la fecha del evento.
     *
     * @return La fecha del asalto.
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * Establece la fecha del evento.
     *
     * @param fecha La fecha a asignar.
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /**
     * Retorna una representación en cadena del objeto Asalto.
     *
     * @return String con detalles del asalto.
     */
    @Override
    public String toString() {
        return "Asalto ["
                + "ID: " + idAsalto
                + ", Fecha: " + fecha
                + ", Asaltante: " + (asaltante != null ? asaltante.getNombreCompleto() + " (Clave: " + asaltante.getClave() + ")" : "Desconocido")
                + ", Sucursal: " + (sucursal != null ? sucursal.getCodigo() : "Desconocida")
                + "]";
    }
}
