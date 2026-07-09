package models;

import java.io.Serializable;

// Represento la relación 1-N con Asaltantes.
public class Banda implements Serializable {
    
    // Añado el identificador de versión para guardar en .txt sin problemas
    private static final long serialVersionUID = 1L;
    
    // Defino los atributos
    private String numeroBanda;
    private int cantMiembros;

    public Banda() {
    }

    // Armo el constructor principal
    public Banda(String numeroBanda, int cantMiembros) {
        this.numeroBanda = numeroBanda;
        this.cantMiembros = cantMiembros;
    }

    // Getters y Setters
    public String getNumeroBanda() {
        return numeroBanda;
    }

    public void setNumeroBanda(String numeroBanda) {
        this.numeroBanda = numeroBanda;
    }

    public int getCantMiembros() {
        return cantMiembros;
    }

    public void setCantMiembros(int cantMiembros) {
        this.cantMiembros = cantMiembros;
    }
    
    // Sobrescribo el método toString para facilitar la visualización
    @Override
    public String toString() {
        return "Banda [" +
               "Número: " + numeroBanda + 
               ", Miembros: " + cantMiembros + 
               "]";
    }
}