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
     * @return El código de la entidad.
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * @param codigo El código a asignar.
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    /**
     * @return El domicilio central de la entidad.
     */
    public String getDomicilioCentral() {
        return domicilioCentral;
    }

    /**
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
