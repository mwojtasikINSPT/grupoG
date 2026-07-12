package models;

import java.io.Serializable;

/**
 * Representa el expediente judicial donde un juez juzga a un asaltante por un
 * asalto específico. Gestiona la vinculación entre el delito, el juez a cargo y
 * el resultado del proceso legal (Relación N:M).
 */
public class CasoJudicial implements Serializable {

    // Añado el identificador de versión para la persistencia en archivos
    private static final long serialVersionUID = 1L;

    private Asalto asalto;
    private Juez juez;
    private boolean condenado;
    private int mesesCarcel;
    //Ver: LocalDate fechaSentencia??

    /**
     * Constructor por defecto.
     */
    public CasoJudicial() {
    }

    /**
     * Construye un nuevo caso judicial.
     *
     * @param asalto El asalto que motiva el caso.
     * @param juez El juez encargado del proceso.
     * @param condenado Indica si el asaltante fue condenado.
     * @param mesesCarcel Cantidad de meses de cárcel (se ajustará a 0 si no hay
     * condena).
     */
    public CasoJudicial(Asalto asalto, Juez juez, boolean condenado, int mesesCarcel) {
        this.asalto = asalto;
        this.juez = juez;
        this.condenado = condenado;
        // Me aseguro de que los meses sean 0 si la persona no fue condenada usando op ternario
        this.mesesCarcel = condenado ? mesesCarcel : 0;
    }

    // Getters y Setters
    /**
     * @return El asalto asociado al caso.
     */
    public Asalto getAsalto() {
        return asalto;
    }

    /**
     * @param asalto El asalto a asociar.
     */
    public void setAsalto(Asalto asalto) {
        this.asalto = asalto;
    }

    /**
     * @return El juez a cargo del caso.
     */
    public Juez getJuez() {
        return juez;
    }

    /**
     * @param juez El juez a asignar.
     */
    public void setJuez(Juez juez) {
        this.juez = juez;
    }

    /**
     * @return true si el asaltante fue condenado, false en caso contrario.
     */
    public boolean isCondenado() {
        return condenado;
    }

    /**
     * @param condenado El estado de condena a establecer. Si es false,
     * mesesCarcel se establece a 0.
     */
    public void setCondenado(boolean condenado) {
        this.condenado = condenado;
        if (!condenado) {
            this.mesesCarcel = 0;
        }
    }

    /**
     * @return Los meses de cárcel dictaminados.
     */
    public int getMesesCarcel() {
        return mesesCarcel;
    }

    /**
     * Establece la cantidad de meses de cárcel. Si la persona no está marcada
     * como condenada, el valor es 0.
     *
     * @param mesesCarcel Cantidad de meses de sentencia.
     */
    public void setMesesCarcel(int mesesCarcel) {
        if (this.condenado) {
            this.mesesCarcel = Math.max(0, mesesCarcel);
        } else {
            this.mesesCarcel = 0;
            System.out.println("Aviso: No se pueden asignar meses de cárcel a un caso sin condena.");
        }
    }

    /**
     * Retorna una representación en cadena del expediente judicial.
     *
     * @return String con los detalles del caso.
     */
    @Override
    public String toString() {
        return "Caso Judicial ["
                + "Asalto ID: " + (asalto != null ? asalto.getIdAsalto() : "Desconocido")
                + ", Juez: " + (juez != null ? juez.getNombre() : "Desconocido")
                + ", Condenado: " + (condenado ? "Sí (" + mesesCarcel + " meses)" : "No")
                + "]";
    }
}
