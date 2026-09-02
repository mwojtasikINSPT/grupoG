package models;

import java.io.Serializable;

/**
 * Representa una sucursal física perteneciente a una entidad bancaria.
 * Guarda solamente el identificador de la entidad relacionada.
 *
 * @author Grupo G
 */
public class Sucursal implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigo;
    private String domicilio;
    private int numeroEmpleados;
    private String idEntidadBancaria;

    /**
     * Crea una sucursal utilizando el identificador de su entidad bancaria.
     *
     * @param codigo código único de la sucursal
     * @param domicilio dirección física de la sucursal
     * @param numeroEmpleados cantidad de empleados
     * @param idEntidadBancaria código de la entidad bancaria
     */
    public Sucursal(
            String codigo,
            String domicilio,
            int numeroEmpleados,
            String idEntidadBancaria) {

        this.codigo = codigo;
        this.domicilio = domicilio;
        setNumeroEmpleados(numeroEmpleados);
        this.idEntidadBancaria = idEntidadBancaria;
    }

    

    /**
     * Obtiene el código de la sucursal.
     *
     * @return código de la sucursal
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Obtiene el domicilio de la sucursal.
     *
     * @return domicilio de la sucursal
     */
    public String getDomicilio() {
        return domicilio;
    }

    /**
     * Asigna el domicilio de la sucursal.
     *
     * @param domicilio nuevo domicilio
     */
    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    /**
     * Obtiene la cantidad de empleados.
     *
     * @return número de empleados
     */
    public int getNumeroEmpleados() {
        return numeroEmpleados;
    }

    /**
     * Asigna la cantidad de empleados.
     *
     * @param numeroEmpleados cantidad de empleados
     * @throws IllegalArgumentException si la cantidad es negativa
     */
    public void setNumeroEmpleados(int numeroEmpleados) {
        if (numeroEmpleados < 0) {
            throw new IllegalArgumentException(
                    "El número de empleados no puede ser negativo."
            );
        }

        this.numeroEmpleados = numeroEmpleados;
    }

    /**
     * Obtiene el código de la entidad bancaria asociada.
     *
     * @return código de la entidad bancaria
     */
    public String getIdEntidadBancaria() {
        return idEntidadBancaria;
    }

    /**
     * Asigna el código de la entidad bancaria.
     *
     * @param idEntidadBancaria código de la entidad bancaria
     */
    public void setIdEntidadBancaria(String idEntidadBancaria) {
        this.idEntidadBancaria = idEntidadBancaria;
    }

    /**
     * Devuelve los datos principales de la sucursal.
     *
     * @return representación textual de la sucursal
     */
    @Override
    public String toString() {
        return "Sucursal ["
                + "Código: " + codigo
                + ", Domicilio: " + domicilio
                + ", Empleados: " + numeroEmpleados
                + ", Entidad bancaria: " + idEntidadBancaria
                + "]";
    }
}