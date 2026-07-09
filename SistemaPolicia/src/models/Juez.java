package models;

//Relacion N-M con asaltantes
public class Juez {
    
    //Atributos
    private String claveInterna;
    private int aniosServicio;
    private String nombre;

    public Juez(String claveInterna, int aniosServicio, String nombre) {
        this.claveInterna = claveInterna;
        this.aniosServicio = aniosServicio;
        this.nombre = nombre;
    }

    public String getClaveInterna() {
        return claveInterna;
    }

    public int getAniosServicio() {
        return aniosServicio;
    }

    public String getNombre() {
        return nombre;
    }
    

}
