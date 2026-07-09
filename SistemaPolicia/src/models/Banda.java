package models;

// Relacion 1-N con Asaltantes.
public class Banda {
    
    //Atributos
    private String numeroBanda;
    private int cantMiembros;

    // 2. Constructor
    public Banda(String numeroBanda, int cantMiembros) {
        this.numeroBanda = numeroBanda;
        this.cantMiembros = cantMiembros;
    }

    // 3. Getters y Setters
    public String getNumeroBanda() {
        return numeroBanda;
    }

    public void setNumeroBanda(String numeroBanda) {
        this.numeroBanda = numeroBanda;
    }

    public int getcantMiembros() {
        return cantMiembros;
    }

    public void setcantMiembros(int cantMiembros) {
        this.cantMiembros = cantMiembros;
    }
}