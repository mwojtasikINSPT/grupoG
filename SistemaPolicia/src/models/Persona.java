package models;

// Clase Abstracta base para entidades con atributos comunes (código/nombre).
public abstract class Persona {
    // protected para que las clases hijas puedan acceder
    protected String codigo;
    protected String nombre;

    // Constructor 
    public Persona(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }
    
    
}
