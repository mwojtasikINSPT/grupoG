package models;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Representa el hecho de un asalto ocurrido en una sucursal.
 *
 * El modelo conserva únicamente los identificadores de las entidades
 * involucradas. Los datos completos deben obtenerse mediante sus respectivos
 * DAO.
 *
 * @author GrupoG
 */
public class Asalto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String idAsalto;
    private String idAsaltante;
    private String idSucursal;
    private LocalDate fecha;

    /**
     * Crea un asalto sin datos iniciales.
     */
    public Asalto() {
    }

    /**
     * Crea un asalto utilizando los identificadores de las entidades
     * involucradas.
     *
     * @param idAsalto identificador único del hecho
     * @param idAsaltante identificador del asaltante involucrado
     * @param idSucursal identificador de la sucursal afectada
     * @param fecha fecha en que ocurrió el hecho
     */
    public Asalto(
            String idAsalto,
            String idAsaltante,
            String idSucursal,
            LocalDate fecha) {

        this.idAsalto = idAsalto;
        this.idAsaltante = idAsaltante;
        this.idSucursal = idSucursal;
        this.fecha = fecha;
    }

    /**
     * Obtiene el identificador del asalto.
     *
     * @return identificador del hecho
     */
    public String getIdAsalto() {
        return idAsalto;
    }

    /**
     * Modifica el identificador del asalto.
     *
     * @param idAsalto nuevo identificador
     */
    public void setIdAsalto(String idAsalto) {
        this.idAsalto = idAsalto;
    }

    /**
     * Obtiene el identificador del asaltante involucrado.
     *
     * @return identificador del asaltante
     */
    public String getIdAsaltante() {
        return idAsaltante;
    }

    /**
     * Modifica el identificador del asaltante involucrado.
     *
     * @param idAsaltante nuevo identificador del asaltante
     */
    public void setIdAsaltante(String idAsaltante) {
        this.idAsaltante = idAsaltante;
    }

    /**
     * Obtiene el identificador de la sucursal afectada.
     *
     * @return identificador de la sucursal
     */
    public String getIdSucursal() {
        return idSucursal;
    }

    /**
     * Modifica el identificador de la sucursal afectada.
     *
     * @param idSucursal nuevo identificador de la sucursal
     */
    public void setIdSucursal(String idSucursal) {
        this.idSucursal = idSucursal;
    }

    /**
     * Obtiene la fecha del asalto.
     *
     * @return fecha del hecho
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * Modifica la fecha del asalto.
     *
     * @param fecha nueva fecha del hecho
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /**
     * Devuelve una descripción legible del asalto.
     *
     * @return datos principales del hecho
     */
    @Override
    public String toString() {
        return "Asalto ["
                + "ID: " + idAsalto
                + ", Fecha: " + fecha
                + ", ID Asaltante: " + idAsaltante
                + ", ID Sucursal: " + idSucursal
                + "]";
    }
}