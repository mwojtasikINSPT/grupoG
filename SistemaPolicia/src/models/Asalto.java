package models;

import java.io.Serializable;
import java.time.LocalDate;

// Resuelve relación N:M. Atributos: Sucursal, Asaltante, fecha
public class Asalto implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String idAsalto;
    private Asaltante asaltante; 
    private Sucursal sucursal;   
    private LocalDate fecha;

    public Asalto() {
    }

    public Asalto(String idAsalto, Asaltante asaltante, Sucursal sucursal, LocalDate fecha) {
        this.idAsalto = idAsalto;
        this.asaltante = asaltante;
        this.sucursal = sucursal;
        this.fecha = fecha;
    }

    public String getIdAsalto() {
        return idAsalto;
    }

    public void setIdAsalto(String idAsalto) {
        this.idAsalto = idAsalto;
    }

    public Asaltante getAsaltante() {
        return asaltante;
    }

    public void setAsaltante(Asaltante asaltante) {
        this.asaltante = asaltante;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    @Override
    public String toString() {
        return "Asalto [" +
               "ID: " + idAsalto + 
               ", Fecha: " + fecha + 
               ", Asaltante: " + (asaltante != null ? asaltante.getNombreCompleto() : "Desconocido") + 
               ", Sucursal: " + (sucursal != null ? sucursal.getCodigo() : "Desconocida") + 
               "]";
    }
}