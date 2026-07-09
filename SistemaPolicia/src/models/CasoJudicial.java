package models;

// Resuelve relación N:M. 
//Representa el expediente donde un juez juzga a un asaltante por un delito.
public class CasoJudicial {

    //Atributos
    private String idAsalto;
    private String claveJuez;
    private boolean condenado;
    private int mesesCarcel; //0 si no hay condena
}
