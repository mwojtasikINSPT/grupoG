package models;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Representa el contrato o turno laboral de un vigilante en una sucursal
 * específica. Resuelve la relación N:M entre Sucursal y Vigilante, indicando la
 * fecha y si el vigilante portaba arma durante dicho turno.
 */
public class ContratoVigilancia implements Serializable {

    // Añado el identificador de versión para la persistencia en archivos
    private static final long serialVersionUID = 1L;

    // Defino los atributos utilizando las referencias a los objetos 
    private Sucursal sucursal;
    private Vigilante vigilante;
    private LocalDate fecha;
    private boolean conArma;

    /**
     * Constructor por defecto.
     */
    public ContratoVigilancia() {
    }

    /**
     * Construye una nueva instancia de ContratoVigilancia.
     *
     * @param sucursal La sucursal donde se presta el servicio.
     * @param vigilante El vigilante contratado para el turno.
     * @param fecha La fecha del turno laboral.
     * @param conArma Indica si el vigilante porta arma durante el turno.
     */
    public ContratoVigilancia(Sucursal sucursal, Vigilante vigilante, LocalDate fecha, boolean conArma) {
        this.sucursal = sucursal;
        this.vigilante = vigilante;
        this.fecha = fecha;
        this.conArma = conArma;
    }

    // Getters y Setters
    /**
     * Obtiene sucursal
     * @return La sucursal del contrato.
     */
    public Sucursal getSucursal() {
        return sucursal;
    }

    /**
     * Establece sucursal
     * @param sucursal La sucursal a asignar.
     */
    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    /**
     * Obtiene vigilante
     * @return El vigilante del contrato.
     */
    public Vigilante getVigilante() {
        return vigilante;
    }

    /**
     * Establece vigilante
     * @param vigilante El vigilante a asignar.
     */
    public void setVigilante(Vigilante vigilante) {
        this.vigilante = vigilante;
    }

    /**
     * Obtiene fecha
     * @return La fecha del contrato.
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * Establece fecha
     * @param fecha La fecha a asignar.
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /**
     * Obtiene arma si o no
     * @return true si el vigilante porta arma, false en caso contrario.
     */
    public boolean isConArma() {
        return conArma;
    }

    /**
     * Establece si tiene o no arma
     * @param conArma El estado de portación de arma a establecer.
     */
    public void setConArma(boolean conArma) {
        this.conArma = conArma;
    }

    /**
     * Retorna una representación en cadena del contrato.
     *
     * @return String con los detalles del contrato.
     */
    @Override
    public String toString() {
        return "Contrato de Vigilancia ["
                + "Sucursal: " + (sucursal != null ? sucursal.getCodigo() : "Desconocida")
                + ", Vigilante: " + (vigilante != null ? vigilante.getCodigo() : "Desconocido")
                + ", Fecha: " + fecha
                + ", Con Arma: " + (conArma ? "Sí" : "No")
                + "]";
    }
}
