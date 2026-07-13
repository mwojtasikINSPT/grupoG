package models;

import java.io.Serializable;

/**
 * Representa a la entidad bancaria central. Mantiene una relación 1:N con las
 * sucursales que dependen de ella.
 */
public class EntidadBancaria implements Serializable {

    private static final long serialVersionUID = 1L;

    //Atributos
    private String codigo;
    private String domicilioCentral;

    /**
     * Constructor por defecto.
     */
    public EntidadBancaria() {
    }

    /**
     * Construye una nueva entidad bancaria.
     *
     * @param codigo Código único de la entidad.
     * @param domicilioCentral Dirección de la sede central.
     */
    public EntidadBancaria(String codigo, String domicilioCentral) {
        this.codigo = codigo;
        this.domicilioCentral = domicilioCentral;
    }

    /**
     * Obtiene el código de la entidad.
     *
     * Obtiene el código de la entidad bancaria.
     *
     * @return El código de la entidad.
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Asigna un nuevo código a la entidad.
     *
     * Asigna un nuevo código a la entidad bancaria.
     *
     * @param codigo El código a asignar.
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    /**
     * Obtiene el domicilio central de la entidad.
     *
     * Obtiene el domicilio central de la entidad bancaria.
     *
     * @return El domicilio central de la entidad.
     */
    public String getDomicilioCentral() {
        return domicilioCentral;
    }

    /**
     * Asigna un nuevo domicilio central a la entidad.
     *
     * Asigna el domicilio central de la entidad bancaria.
     *
     * @param domicilioCentral La dirección a asignar.
     */
    public void setDomicilioCentral(String domicilioCentral) {
        this.domicilioCentral = domicilioCentral;
    }

    /**
     * Retorna una representación en cadena de la entidad bancaria.
     *
     * @return String con los datos de la entidad.
     */
    @Override
    public String toString() {
        return "Entidad Bancaria ["
                + "Código: " + codigo
                + ", Domicilio Central: " + domicilioCentral
                + "]";
    }
}
