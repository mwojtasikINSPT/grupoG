package models;

// Resuelve relación N:M. 
//Representa el expediente donde un juez juzga a un asaltante por un delito.
public class CasoJudicial {

    //Atributos
    private String idAsalto;
    private String claveJuez;
    private boolean condenado;
    private int mesesCarcel; //0 si no hay condena

    public CasoJudicial(String idAsalto, String claveJuez, boolean condenado, int mesesCarcel) {
        this.idAsalto = idAsalto;
        this.claveJuez = claveJuez;
        this.condenado = condenado;
        this.mesesCarcel = mesesCarcel;
    }

    public String getIdAsalto() {
        return idAsalto;
    }

    public String getClaveJuez() {
        return claveJuez;
    }

    public boolean isCondenado() {
        return condenado;
    }

    public int getMesesCarcel() {
        return mesesCarcel;
    }

    public void setClaveJuez(String claveJuez) {
        this.claveJuez = claveJuez;
    }

    public void setCondenado(boolean condenado) {
        this.condenado = condenado;
    }

    public void setMesesCarcel(int mesesCarcel) {
        this.mesesCarcel = mesesCarcel;
    }
    
    
}
