package models;

import java.io.Serializable;
import java.time.LocalDate;

// Resuelvo la relación N:M. 
// Represento el turno laboral de un vigilante en una sucursal específica en un día concreto.
public class ContratoVigilancia implements Serializable {
    
    // Añado el identificador de versión para la persistencia en archivos
    private static final long serialVersionUID = 1L;
    
    // Defino los atributos utilizando las referencias a los objetos 
    private Sucursal sucursal;
    private Vigilante vigilante;
    private LocalDate fecha;
    private boolean conArma;

    // Constructor vacío
    public ContratoVigilancia() {
    }

    // Constructor principal
    public ContratoVigilancia(Sucursal sucursal, Vigilante vigilante, LocalDate fecha, boolean conArma) {
        this.sucursal = sucursal;
        this.vigilante = vigilante;
        this.fecha = fecha;
        this.conArma = conArma;
    }

    // Getters y Setters
    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public Vigilante getVigilante() {
        return vigilante;
    }

    public void setVigilante(Vigilante vigilante) {
        this.vigilante = vigilante;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public boolean isConArma() {
        return conArma;
    }

    public void setConArma(boolean conArma) {
        this.conArma = conArma;
    }
    
    // Sobrescribo el método para visualizar el contrato
    @Override
    public String toString() {
        return "Contrato de Vigilancia [" +
               "Sucursal: " + (sucursal != null ? sucursal.getCodigo() : "Desconocida") + 
               ", Vigilante: " + (vigilante != null ? vigilante.getCodigo() : "Desconocido") + 
               ", Fecha: " + fecha + 
               ", Con Arma: " + (conArma ? "Sí" : "No") + 
               "]";
    }
}