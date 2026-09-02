package models;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Representa un servicio de vigilancia realizado en una sucursal.
 * Guarda únicamente los identificadores de las personas y lugares
 * relacionados con el hecho.
 *
 * @author Grupo G
 */
public class ContratoVigilancia implements Serializable {

    private static final long serialVersionUID = 1L;

    private String idSucursal;
    private String idVigilante;
    private LocalDate fecha;
    private boolean conArma;

    /**
     * Crea un contrato vacío.
     */
    public ContratoVigilancia() {
    }

    /**
     * Crea un contrato utilizando los identificadores relacionados.
     *
     * @param idSucursal código de la sucursal
     * @param idVigilante código del vigilante
     * @param fecha fecha del servicio
     * @param conArma indica si el vigilante porta un arma
     */
    public ContratoVigilancia(
            String idSucursal,
            String idVigilante,
            LocalDate fecha,
            boolean conArma) {

        this.idSucursal = idSucursal;
        this.idVigilante = idVigilante;
        this.fecha = fecha;
        this.conArma = conArma;
    }



    /**
     * Obtiene el código de la sucursal.
     *
     * @return código de la sucursal
     */
    public String getIdSucursal() {
        return idSucursal;
    }

    /**
     * Asigna el código de la sucursal.
     *
     * @param idSucursal código de la sucursal
     */
    public void setIdSucursal(String idSucursal) {
        this.idSucursal = idSucursal;
    }

    /**
     * Obtiene el código del vigilante.
     *
     * @return código del vigilante
     */
    public String getIdVigilante() {
        return idVigilante;
    }

    /**
     * Asigna el código del vigilante.
     *
     * @param idVigilante código del vigilante
     */
    public void setIdVigilante(String idVigilante) {
        this.idVigilante = idVigilante;
    }

    /**
     * Obtiene la fecha del servicio.
     *
     * @return fecha del contrato
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * Asigna la fecha del servicio.
     *
     * @param fecha fecha del contrato
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /**
     * Indica si el vigilante porta un arma.
     *
     * @return {@code true} si porta un arma
     */
    public boolean isConArma() {
        return conArma;
    }

    /**
     * Establece si el vigilante porta un arma.
     *
     * @param conArma estado de portación de arma
     */
    public void setConArma(boolean conArma) {
        this.conArma = conArma;
    }

    /**
     * Devuelve los datos principales del contrato.
     *
     * @return representación textual del contrato
     */
    @Override
    public String toString() {
        return "Contrato de Vigilancia ["
                + "Sucursal: " + idSucursal
                + ", Vigilante: " + idVigilante
                + ", Fecha: " + fecha
                + ", Con arma: " + (conArma ? "Sí" : "No")
                + "]";
    }
}