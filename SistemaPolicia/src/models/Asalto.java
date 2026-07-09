package models;

// Resuelve relación N:M. Atributos: Sucursal, Asaltante, fecha

import java.time.LocalDate;

public class Asalto {
    private String idAsalto;
    private String claveAsaltante;
    private String codigoSucursal;
    private LocalDate fecha;

    public Asalto(String idAsalto, String claveAsaltante, String codigoSucursal, LocalDate fecha) {
        this.idAsalto = idAsalto;
        this.claveAsaltante = claveAsaltante;
        this.codigoSucursal = codigoSucursal;
        this.fecha = fecha;
    }

    public String getIdAsalto() {
        return idAsalto;
    }

    public String getClaveAsaltante() {
        return claveAsaltante;
    }

    public String getCodigoSucursal() {
        return codigoSucursal;
    }

    public LocalDate getFecha() {
        return fecha;
    }


    public void setClaveAsaltante(String claveAsaltante) {
        this.claveAsaltante = claveAsaltante;
    }


    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    
}
