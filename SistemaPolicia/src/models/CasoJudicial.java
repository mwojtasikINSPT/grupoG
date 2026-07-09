package models;

import java.io.Serializable;

// Resuelvo la relación N:M. 
// Represento el expediente donde un juez juzga a un asaltante por un delito (Asalto).
public class CasoJudicial implements Serializable {

    // Añado el identificador de versión para la persistencia en archivos
    private static final long serialVersionUID = 1L;

    // Atributos
    private Asalto asalto;
    private Juez juez;
    private boolean condenado;
    private int mesesCarcel; // 0 si no hay condena

    public CasoJudicial() {
    }

    // Constructor 
    public CasoJudicial(Asalto asalto, Juez juez, boolean condenado, int mesesCarcel) {
        this.asalto = asalto;
        this.juez = juez;
        this.condenado = condenado;
        // Me aseguro de que los meses sean 0 si la persona no fue condenada usando op ternario
        this.mesesCarcel = condenado ? mesesCarcel : 0; 
    }

    // Getters y Setters
    public Asalto getAsalto() {
        return asalto;
    }

    public void setAsalto(Asalto asalto) {
        this.asalto = asalto;
    }

    public Juez getJuez() {
        return juez;
    }

    public void setJuez(Juez juez) {
        this.juez = juez;
    }

    public boolean isCondenado() {
        return condenado;
    }

    public void setCondenado(boolean condenado) {
        this.condenado = condenado;
        if (!condenado) {
            this.mesesCarcel = 0;
        }
    }

    public int getMesesCarcel() {
        return mesesCarcel;
    }

    public void setMesesCarcel(int mesesCarcel) {
        this.mesesCarcel = mesesCarcel;
    }
    
    // Sobrescribo el método para visualizar el expediente 
    @Override
    public String toString() {
        return "Caso Judicial [" +
               "Asalto ID: " + (asalto != null ? asalto.getIdAsalto() : "Desconocido") + 
               ", Juez: " + (juez != null ? juez.getNombre() : "Desconocido") + 
               ", Condenado: " + (condenado ? "Sí (" + mesesCarcel + " meses)" : "No") + 
               "]";
    }
}
