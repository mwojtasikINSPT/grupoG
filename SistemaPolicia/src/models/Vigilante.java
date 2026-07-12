package models;

import java.io.Serializable;

/**
 * Representa a un vigilante encargado de la seguridad. La relación N-M con las
 * sucursales bancarias se resuelve mediante la clase ContratoVigilancia,
 * registrando las fechas de contratación y el uso de arma.
 */
public class Vigilante implements Serializable {

    // Añado el identificador de versión para la persistencia en archivos
    private static final long serialVersionUID = 1L;

    private String codigo;
    private int edad;

    public Vigilante() {
    }

    /**
     * Construye un nuevo vigilante con sus datos básicos.
     *
     * @param codigo Código identificador único del vigilante.
     * @param edad Edad del vigilante.
     */
    public Vigilante(String codigo, int edad) {
        this.codigo = codigo;
        this.edad = edad;
    }

    // Getters y Setters
    /**
     * Obtiene el código identificador del vigilante.
     *
     * @return El código.
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Establece el código identificador del vigilante.
     *
     * @param codigo El código a asignar.
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    /**
     * Obtiene la edad del vigilante.
     *
     * @return La edad.
     */
    public int getEdad() {
        return edad;
    }

    /**
     * Establece la edad del vigilante.
     *
     * @param edad La edad a asignar.
     */
    public void setEdad(int edad) {
        this.edad = edad;
    }

    /**
     * Retorna una representación en cadena del vigilante.
     *
     * @return String con los datos del vigilante.
     */
    @Override
    public String toString() {
        return "Vigilante ["
                + "Código: " + codigo
                + ", Edad: " + edad
                + "]";
    }
}
