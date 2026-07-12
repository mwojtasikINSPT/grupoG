package models;

import java.io.Serializable;

/**
 * Representa a un juez encargado de los casos judiciales de los asaltantes. La
 * relación N-M con asaltantes se resuelve mediante la clase CasoJudicial.
 */
public class Juez implements Serializable {

    // Añado el identificador de versión para poder guardar en archivo .txt
    private static final long serialVersionUID = 1L;

    // Atributos
    private String claveInterna;
    private int aniosServicio;
    private String nombre;

    public Juez() {
    }

    /**
     * Construye un nuevo juez con sus datos identificatorios.
     *
     * @param claveInterna Clave interna del juzgado.
     * @param aniosServicio Cantidad de años de servicio.
     * @param nombre Nombre completo del juez.
     */
    public Juez(String claveInterna, int aniosServicio, String nombre) {
        this.claveInterna = claveInterna;
        this.aniosServicio = aniosServicio;
        this.nombre = nombre;
    }

    // Getters y Setters
    /**
     * Obtiene la clave interna del juzgado.
     *
     * @return La clave interna.
     */
    public String getClaveInterna() {
        return claveInterna;
    }

    /**
     * Establece la clave interna del juzgado.
     *
     * @param claveInterna La clave a asignar.
     */
    public void setClaveInterna(String claveInterna) {
        this.claveInterna = claveInterna;
    }

    /**
     * Obtiene los años de servicio.
     *
     * @return Los años de servicio.
     */
    public int getAniosServicio() {
        return aniosServicio;
    }

    /**
     * Establece los años de servicio.
     *
     * @param aniosServicio Los años a asignar.
     */
    public void setAniosServicio(int aniosServicio) {
        this.aniosServicio = aniosServicio;
    }

    /**
     * Obtiene el nombre del juez.
     *
     * @return El nombre del juez.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del juez.
     *
     * @param nombre El nombre a asignar.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Retorna una representación en cadena del juez.
     *
     * @return String con los datos del juez.
     */
    @Override
    public String toString() {
        return "Juez ["
                + "Clave: " + claveInterna
                + ", Nombre: " + nombre
                + ", Años de Servicio: " + aniosServicio
                + "]";
    }
}
