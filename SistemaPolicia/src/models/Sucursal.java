package models;

// Relacion 1-1 con EntidadBancaria. Representa el lugar fisico al que pertenece la entidad 

public class Sucursal {
    // 1. Atributos  
    private String codigo;
    private String domicilio;
    private int numeroEmpleados;
    private String codigoEntidad;

    // 2. Constructor 
    public Sucursal(String codigo, String domicilio, int numeroEmpleados, String codigoEntidad) {
        this.codigo = codigo;
        this.domicilio = domicilio;
        this.numeroEmpleados = numeroEmpleados;
        this.codigoEntidad = codigoEntidad;
    }

    // 3. Getters y Setters 
    public void setNumeroEmpleados(int numeroEmpleados) {
        
        if (numeroEmpleados >= 0) {
            this.numeroEmpleados = numeroEmpleados;
        } else {
            System.out.println("Error: El número de empleados no puede ser negativo.");
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

    public String getCodigoEntidad() {
        return codigoEntidad;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    
    
}
