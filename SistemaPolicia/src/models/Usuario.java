package models;

//  para el control de acceso y definir comportamientos.
public abstract class Usuario {
    //Atributos
    private String username;
    private String password;
    
    //Constructor
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

    // Método abstracto para los obtener roles (en enum)
    public abstract Rol obtenerRol();
}
