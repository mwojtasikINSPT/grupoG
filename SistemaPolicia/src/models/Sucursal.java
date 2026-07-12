package models;

import java.io.Serializable;

/**
 * Representa una sucursal física de una entidad bancaria. Mantiene una relación
 * 1:N con la entidad bancaria a la que pertenece.
 */
public class Sucursal implements Serializable {

    //Nro version de la estructura, Necesario si mas adelante agrego atributos a la clase
    private static final long serialVersionUID = 1L;

    //Atributos  
    private String codigo;
    private String domicilio;
    private int numeroEmpleados;
    private EntidadBancaria entidad;

    /**
     * Construye una nueva sucursal.
     *
     * @param codigo Código único de la sucursal.
     * @param domicilio Dirección física de la sucursal.
     * @param numeroEmpleados Cantidad de empleados en la sucursal.
     * @param entidad La entidad bancaria a la que pertenece.
     */
    public Sucursal(String codigo, String domicilio, int numeroEmpleados, EntidadBancaria entidad) {
        this.codigo = codigo;
        this.domicilio = domicilio;
        setNumeroEmpleados(numeroEmpleados); // Llamo al setter para que valide al crear
        this.entidad = entidad;
    }

    /**
     * Establece el número de empleados, validando que no sea negativo.
     *
     * @param numeroEmpleados Cantidad de empleados a asignar.
     * @throws IllegalArgumentException si el valor es menor a cero.
     */
    public void setNumeroEmpleados(int numeroEmpleados) {
        if (numeroEmpleados >= 0) {
            this.numeroEmpleados = numeroEmpleados;
        } else {
            // MVC: Lanzo excepción
            throw new IllegalArgumentException("Error: El número de empleados no puede ser negativo.");
        }
    }

    /**
     * @return El código único de la sucursal.
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * @return El domicilio de la sucursal.
     */
    public String getDomicilio() {
        return domicilio;
    }

    /**
     * @return El número de empleados.
     */
    public int getNumeroEmpleados() {
        return numeroEmpleados;
    }

    /**
     * @return La entidad bancaria asociada.
     */
    public EntidadBancaria getEntidad() {
        return entidad;
    }

    /**
     * @param domicilio El domicilio a asignar.
     */
    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }
}
