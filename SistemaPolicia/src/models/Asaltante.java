package models;

import java.io.Serializable;

// Relacion N-M con Sucursales (a través de Asalto). Relación N-1 con Banda.
public class Asaltante implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Atributos
    private String clave;
    private String nombreCompleto;
    private Banda banda; 

    // Constructor vacío
    public Asaltante() {
    }

    public Asaltante(String clave, String nombreCompleto, Banda banda) {
        this.clave = clave;
        this.nombreCompleto = nombreCompleto;
        this.banda = banda;
    }

    // Getters y Setters
    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public Banda getBanda() {
        return banda;
    }

    public void setBanda(Banda banda) {
        this.banda = banda;
    }
    
    @Override
    public String toString() {
        // Valido si tiene banda o es null para evitar errores al imprimir
        String infoBanda = (banda != null) ? String.valueOf(banda.getNumeroBanda()) : "Ninguna (Actúa solo)";
        return "Asaltante [" +
               "Clave: " + clave + 
               ", Nombre: " + nombreCompleto + 
               ", Banda N°: " + infoBanda + 
               "]";
    }
}