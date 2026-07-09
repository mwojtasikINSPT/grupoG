package models;

import java.io.Serializable;

// Relacion N-1 con EntidadBancaria, pero cada Sucursal pertenece SOLO a una entidad. Representa el lugar fisico al que pertenece la entidad.
public class Sucursal implements Serializable {
    
    //Nro version de la estructura, Necesario si mas adelante agrego atributos a la clase
    private static final long serialVersionUID = 1L;

    // 1. Atributos  
    private String codigo;
    private String domicilio;
    private int numeroEmpleados;
    private EntidadBancaria entidad;

    // 2. Constructor 
    public Sucursal(String codigo, String domicilio, int numeroEmpleados, EntidadBancaria entidad) {
        this.codigo = codigo;
        this.domicilio = domicilio;
        setNumeroEmpleados(numeroEmpleados); // Llamo al setter para que valide al crear
        this.entidad = entidad;
    }

    // 3. Getters y Setters 
    public void setNumeroEmpleados(int numeroEmpleados) {
        if (numeroEmpleados >= 0) {
            this.numeroEmpleados = numeroEmpleados;
        } else {
            // MVC: Lanzo excepción
            throw new IllegalArgumentException("Error: El número de empleados no puede ser negativo.");
        }
    }
    
    public String getCodigo() {
        return codigo;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public int getNumeroEmpleados() {
        return numeroEmpleados;
    }

    public EntidadBancaria getEntidad() {
        return entidad;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }
}