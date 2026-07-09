package models;

import java.io.Serializable;

// Aclaro que la relación N-M con asaltantes se resuelve mediante la clase CasoJudicial
public class Juez implements Serializable {
    
    // Añado el identificador de versión para poder guardar en archivo .txt
    private static final long serialVersionUID = 1L;
    
    // Atributos
    private String claveInterna;
    private int aniosServicio;
    private String nombre;
    public Juez() {
    }

    // Constructor
    public Juez(String claveInterna, int aniosServicio, String nombre) {
        this.claveInterna = claveInterna;
        this.aniosServicio = aniosServicio;
        this.nombre = nombre;
    }

    // Getters y Setters
    public String getClaveInterna() {
        return claveInterna;
    }

    public void setClaveInterna(String claveInterna) {
        this.claveInterna = claveInterna;
    }

    public int getAniosServicio() {
        return aniosServicio;
    }

    public void setAniosServicio(int aniosServicio) {
        this.aniosServicio = aniosServicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    // Sobrescribo el método para mostrar los datos en consola
    @Override
    public String toString() {
        return "Juez [" +
               "Clave: " + claveInterna + 
               ", Nombre: " + nombre + 
               ", Años de Servicio: " + aniosServicio + 
               "]";
    }
}