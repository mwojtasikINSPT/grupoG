package models;

// Relacion N-M con Sucursales
public class Vigilante {

    //Atributos
    private String codigo;
    private int edad;

    // Constructor
    public Vigilante(String codigo, int edad) {
        this.codigo = codigo;
        this.edad = edad;
    }

    // Getters y Setters
    public String getCodigo() {
        return codigo;
    }


    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }
}
