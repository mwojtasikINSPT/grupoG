package models;

// Hereda de Persona. Añade atributo: edad.
public class Vigilante extends Persona{
    private int edad;
    private Usuario cuentaUsuario; // Composición: El vigilante "tiene" una cuenta para entrar al sistema
    
    public Vigilante(String codigo, String nombre, int edad, Usuario cuentaUsuario) {
        super(codigo, nombre);
        this.edad = edad;
        this.cuentaUsuario = cuentaUsuario;
    }
}
