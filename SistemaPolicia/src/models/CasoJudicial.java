package models;

import java.io.Serializable;

/**
 * Representa el resultado judicial asociado con un asalto.
 *
 * El modelo conserva únicamente los identificadores del asalto y del juez.
 * Los datos completos deben consultarse mediante sus respectivos DAO.
 *
 * @author GrupoG
 */
public class CasoJudicial implements Serializable {

    private static final long serialVersionUID = 1L;

    private String idAsalto;
    private String idJuez;
    private boolean condenado;
    private int mesesCarcel;

    /**
     * Crea un caso judicial sin datos iniciales.
     */
    public CasoJudicial() {
    }

    /**
     * Crea un caso judicial utilizando los identificadores relacionados.
     *
     * @param idAsalto identificador del asalto
     * @param idJuez identificador del juez responsable
     * @param condenado indica si existió una condena
     * @param mesesCarcel duración de la condena en meses
     */
    public CasoJudicial(
            String idAsalto,
            String idJuez,
            boolean condenado,
            int mesesCarcel) {

        this.idAsalto = idAsalto;
        this.idJuez = idJuez;
        this.condenado = condenado;
        this.mesesCarcel = condenado
                ? Math.max(0, mesesCarcel)
                : 0;
    }

    /**
     * Obtiene el identificador del asalto relacionado.
     *
     * @return identificador del asalto
     */
    public String getIdAsalto() {
        return idAsalto;
    }

    /**
     * Modifica el identificador del asalto relacionado.
     *
     * @param idAsalto nuevo identificador del asalto
     */
    public void setIdAsalto(String idAsalto) {
        this.idAsalto = idAsalto;
    }

    /**
     * Obtiene el identificador del juez responsable.
     *
     * @return identificador del juez
     */
    public String getIdJuez() {
        return idJuez;
    }

    /**
     * Modifica el identificador del juez responsable.
     *
     * @param idJuez nuevo identificador del juez
     */
    public void setIdJuez(String idJuez) {
        this.idJuez = idJuez;
    }

    /**
     * Indica si el caso terminó con una condena.
     *
     * @return {@code true} si existió una condena
     */
    public boolean isCondenado() {
        return condenado;
    }

    /**
     * Modifica el resultado del caso.
     *
     * Al retirar la condena, la cantidad de meses se restablece a cero.
     *
     * @param condenado nuevo resultado
     */
    public void setCondenado(boolean condenado) {
        this.condenado = condenado;

        if (!condenado) {
            this.mesesCarcel = 0;
        }
    }

    /**
     * Obtiene la duración de la condena.
     *
     * @return cantidad de meses de cárcel
     */
    public int getMesesCarcel() {
        return mesesCarcel;
    }

    /**
     * Modifica la duración de la condena.
     *
     * Cuando no existe condena, el valor permanece en cero.
     *
     * @param mesesCarcel nueva cantidad de meses
     */
    public void setMesesCarcel(int mesesCarcel) {
        this.mesesCarcel = condenado
                ? Math.max(0, mesesCarcel)
                : 0;
    }

    /**
     * Devuelve una descripción legible del caso judicial.
     *
     * @return datos principales del caso
     */
    @Override
    public String toString() {
        return "Caso Judicial ["
                + "ID Asalto: " + idAsalto
                + ", ID Juez: " + idJuez
                + ", Condenado: " + condenado
                + ", Meses de cárcel: " + mesesCarcel
                + "]";
    }
}
