package models;

import java.io.Serializable;

// Defino el control de acceso y los comportamientos base para los distintos perfiles.
public abstract class Usuario implements Serializable {
    
    // Añado el identificador de versión para la persistencia
    private static final long serialVersionUID = 1L;

    // Defino los atributos comunes a todos los usuarios
    private String username;
    private String password;
    
    public Usuario() {
    }

    // Constructor principal
    public Usuario(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters y Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Defino el método abstracto para obtener el rol (en enum) desde las clases hijas
    public abstract Rol obtenerRol();
}