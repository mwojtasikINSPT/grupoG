package models;

// Representa al Banco. Relacion 1-N con Sucursales.
public class EntidadBancaria {
    
    private String codigo;
    private String domicilioCentral;

    public EntidadBancaria(String codigo, String domicilioCentral) {
        this.codigo = codigo;
        this.domicilioCentral = domicilioCentral;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDomicilioCentral() {
        return domicilioCentral;
    }

    public void setDomicilioCentral(String domicilioCentral) {
        this.domicilioCentral = domicilioCentral;
    }

    
}
