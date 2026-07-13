package dtos;

/**
 * Objeto de transferencia de datos (DTO) que representa el detalle consolidado
 * de un delito específico, vinculando información del asalto, la sucursal
 * involucrada y el estado judicial.
 */
public class DetalleDelitoDTO {

    /**
     * Identificador único del asalto.
     */
    private final String idAsalto;

    /**
     * Fecha en la que ocurrió el asalto.
     */
    private final String fechaAsalto;

    /**
     * Información concatenada de la sucursal (ID y domicilio).
     */
    private final String detalleSucursal;

    /**
     * Nombre del juez asignado a la causa.
     */
    private final String nombreJuez;

    /**
     * Veredicto dictado sobre el caso.
     */
    private final String veredicto;

    /**
     * Construye un nuevo DetalleDelitoDTO con la información del delito.
     *
     * @param idAsalto Identificador del asalto.
     * @param fechaAsalto Fecha del suceso.
     * @param detalleSucursal Datos de la sucursal afectada.
     * @param nombreJuez Nombre del magistrado interviniente.
     * @param veredicto Resultado del proceso judicial.
     */
    public DetalleDelitoDTO(String idAsalto, String fechaAsalto, String detalleSucursal, String nombreJuez, String veredicto) {
        this.idAsalto = idAsalto;
        this.fechaAsalto = fechaAsalto;
        this.detalleSucursal = detalleSucursal;
        this.nombreJuez = nombreJuez;
        this.veredicto = veredicto;
    }

    /**
     * Obtiene id del Asalto
     * Obtiene el identificador del asalto.
     *
     * @return El identificador del asalto.
     */
    public String getIdAsalto() {
        return idAsalto;
    }

    /**
     * Establece id del Asalto
     * Obtiene la fecha en la que ocurrió el asalto.
     *
     * @return La fecha en la que ocurrió el asalto.
     */
    public String getFechaAsalto() {
        return fechaAsalto;
    }

    /**
     * Obtiene detalle sucursal
     * Obtiene el detalle de la sucursal afectada.
     *
     * @return El detalle de la sucursal (ID y domicilio).
     */
    public String getDetalleSucursal() {
        return detalleSucursal;
    }

    /**
     * Obtiene nombre juez
     * Obtiene el nombre del juez a cargo.
     *
     * @return El nombre del juez a cargo.
     */
    public String getNombreJuez() {
        return nombreJuez;
    }

    /**
     * Obtiene sentencia o veredicto
     * Obtiene el veredicto del caso.
     *
     * @return El veredicto del caso.
     */
    public String getVeredicto() {
        return veredicto;
    }
}
