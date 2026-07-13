package models;

import java.io.Serializable;

/**
 * Clase abstracta que representa a un usuario del sistema. Define los atributos
 * comunes para la autenticación y obliga a las subclases a definir su rol
 * específico mediante el método {@link #obtenerRol()}.
 */
public abstract class Usuario implements Serializable {

    // Añado el identificador de versión para la persistencia
    private static final long serialVersionUID = 1L;

    // Defino los atributos comunes a todos los usuarios
    private String username;
    private String password;

    /**
     * Constructor por defecto.
     */
    public Usuario() {
    }

    /**
     * Construye un nuevo usuario.
     *
     * @param username Nombre de usuario para el acceso.
     * @param password Contraseña asociada a la cuenta.
     */
    public Usuario(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters y Setters
    /**
     * Obtiene el nombre de usuario almacenado en la cuenta.
     *
     * @return El nombre de usuario.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Asigna un nuevo nombre de usuario a la cuenta.
     *
     * @param username El nombre de usuario a asignar.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Obtiene la contraseña registrada para el usuario.
     *
     * @return La contraseña del usuario.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Asigna una nueva contraseña a la cuenta.
     *
     * @param password La contraseña a asignar.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Método abstracto para obtener el rol del usuario. Cada subclase debe
     * implementar su propio rol.
     *
     * * @return El {@link Rol} correspondiente al usuario.
     */
    public abstract Rol obtenerRol();
}
