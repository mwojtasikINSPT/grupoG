package models;

import java.io.Serializable;

/**
 * Representa una banda organizada a la cual pueden pertenecer los
 * asaltantes.(Relación 1-N) Permite agrupar delincuentes para el control de la
 * Policía Federal.
 */
public class Banda implements Serializable {

    /**
     * Identificador único de versión para la serialización. (para guardar en
     * .txt sin problemas)
     */
    private static final long serialVersionUID = 1L;

    private String numeroBanda;
    private int cantMiembros;

    /**
     * Constructor por defecto
     */
    public Banda() {
    }

    /**
     * Construye una nueva banda con un número identificador.
     *
     * @param numeroBanda Identificador único de la banda.
     * @param cantMiembros Cantidad inicial de miembros.
     */
    public Banda(String numeroBanda, int cantMiembros) {
        this.numeroBanda = numeroBanda;
        this.cantMiembros = cantMiembros;
    }

    // Getters y Setters
    /**
     * Obtiene el número de identificación de la banda.
     *
     * @return El identificador de la banda.
     */
    public String getNumeroBanda() {
        return numeroBanda;
    }

    /**
     * Establece el número de identificación de la banda.
     *
     * @param numeroBanda El identificador único a asignar.
     */
    public void setNumeroBanda(String numeroBanda) {
        this.numeroBanda = numeroBanda;
    }

    /**
     * Obtiene la cantidad de miembros de la banda.
     *
     * @return El número de miembros.
     */
    public int getCantMiembros() {
        return cantMiembros;
    }

    /**
     * Establece la cantidad de miembros de la banda.
     *
     * @param cantMiembros El número de miembros a asignar.
     */
    public void setCantMiembros(int cantMiembros) {
        this.cantMiembros = cantMiembros;
    }

    /**
     * Retorna una representación en cadena de texto de la banda.
     *
     * @return Una cadena con los datos de la banda.
     */
    @Override
    public String toString() {
        return "Banda ["
                + "Número: " + numeroBanda
                + ", Miembros: " + cantMiembros
                + "]";
    }
}
