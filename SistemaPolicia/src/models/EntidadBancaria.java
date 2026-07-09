package models;

import java.io.Serializable;

// Representa al Banco. Relacion 1-N con Sucursales.
public class EntidadBancaria implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    //Atributos
    private String codigo;
    private String domicilioCentral;

    // Constructor vacío 
    public EntidadBancaria() {
    }

    public EntidadBancaria(String codigo, String domicilioCentral) {
        this.codigo = codigo;
        this.domicilioCentral = domicilioCentral;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDomicilioCentral() {
        return domicilioCentral;
    }

    public void setDomicilioCentral(String domicilioCentral) {
        this.domicilioCentral = domicilioCentral;
    }
    
    @Override
    public String toString() {
        return "Entidad Bancaria [" + 
               "Código: " + codigo + 
               ", Domicilio Central: " + domicilioCentral + 
               "]";
    }
}