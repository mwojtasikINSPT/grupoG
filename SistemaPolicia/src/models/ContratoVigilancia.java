package models;

import java.time.LocalDate;

// Resuelve relación N:M. 
//Representa el turno laboral de un vigilante en una sucursal específica en un día concreto.
public class ContratoVigilancia {
    
    private String codigoSucursal;
    private String codigoVigilante;
    private LocalDate fecha;
    private boolean conArma;

    public ContratoVigilancia(String codigoSucursal, String codigoVigilante, LocalDate fecha, boolean conArma) {
        this.codigoSucursal = codigoSucursal;
        this.codigoVigilante = codigoVigilante;
        this.fecha = fecha;
        this.conArma = conArma;
    }

    public String getCodigoSucursal() {
        return codigoSucursal;
    }

    public String getCodigoVigilante() {
        return codigoVigilante;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public boolean isConArma() {
        return conArma;
    }

    public void setCodigoSucursal(String codigoSucursal) {
        this.codigoSucursal = codigoSucursal;
    }

    public void setCodigoVigilante(String codigoVigilante) {
        this.codigoVigilante = codigoVigilante;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setConArma(boolean conArma) {
        this.conArma = conArma;
    }
    
    
    
}
