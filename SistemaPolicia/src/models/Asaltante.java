package models;

//Relacion N-M con Sucursales. Añade relación con Banda.
public class Asaltante {
    
    //Atributos
    private String clave;
    private String nombreCompleto;
    private String Banda;

    public Asaltante(String clave, String nombreCompleto, String Banda) {
        this.clave = clave;
        this.nombreCompleto = nombreCompleto;
        this.Banda = Banda;
    }

    public String getClave() {
        return clave;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getBanda() {
        return Banda;
    }

 
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public void setBanda(String Banda) {
        this.Banda = Banda;
    }
    
    
    
       
}
