package models;

import java.io.Serializable;

// Resuelvo la relación N-M con Sucursales a través de la clase ContratoVigilancia.
public class Vigilante implements Serializable {
    
    // Añado el identificador de versión para la persistencia en archivos
    private static final long serialVersionUID = 1L;

    // Defino los atributos
    private String codigo;
    private int edad;

    // Constructor vacío
    public Vigilante() {
    }

    // Constructor principal
    public Vigilante(String codigo, int edad) {
        this.codigo = codigo;
        this.edad = edad;
    }

    // Getters y Setters
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }
    
    // Sobrescribo el método para visualizar al vigilante 
    @Override
    public String toString() {
        return "Vigilante [" +
               "Código: " + codigo + 
               ", Edad: " + edad + 
               "]";
    }
}