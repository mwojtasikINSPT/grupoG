package dtos;

/**
 * Objeto de transferencia de datos (DTO) diseñado para proporcionar al
 * vigilante únicamente la información relevante de su contrato, vinculando sus
 * datos personales con los detalles de la sucursal asignada y las condiciones
 * laborales.
 */
public class VigilanteContratoDTO {

    /**
     * Código único del vigilante.
     */
    private final String codigoVigilante;

    /**
     * Edad actual del vigilante.
     */
    private int edadVigilante;

    /**
     * Código de la sucursal asignada.
     */
    private String codigoSucursal;

    /**
     * Dirección física de la sucursal.
     */
    private String domicilioSucursal;

    /**
     * Fecha de vigencia del contrato o asignación.
     */
    private String fecha;

    /**
     * Condición de portación de arma del vigilante ("SI" o "NO").
     */
    private String portacionArma;

    /**
     * Construye un nuevo VigilanteContratoDTO.
     *
     * * @param codigoVigilante Identificador del vigilante.
     * @param edadVigilante Edad del vigilante.
     * @param codigoSucursal Identificador de la sucursal.
     * @param domicilioSucursal Ubicación de la sucursal.
     * @param fecha Fecha de la asignación.
     * @param portacionArma Estado de portación de arma.
     */
    public VigilanteContratoDTO(String codigoVigilante, int edadVigilante, String codigoSucursal, String domicilioSucursal, String fecha, String portacionArma) {
        this.codigoVigilante = codigoVigilante;
        this.edadVigilante = edadVigilante;
        this.codigoSucursal = codigoSucursal;
        this.domicilioSucursal = domicilioSucursal;
        this.fecha = fecha;
        this.portacionArma = portacionArma;
    }

    /**
     * @return El código del vigilante.
     */
    public String getCodigoVigilante() {
        return codigoVigilante;
    }

    /**
     * @return La edad del vigilante.
     */
    public int getEdadVigilante() {
        return edadVigilante;
    }

    /**
     * @return El código de la sucursal asignada.
     */
    public String getCodigoSucursal() {
        return codigoSucursal;
    }

    /**
     * @return El domicilio de la sucursal.
     */
    public String getDomicilioSucursal() {
        return domicilioSucursal;
    }

    /**
     * @return La fecha de la asignación.
     */
    public String getFecha() {
        return fecha;
    }

    /**
     * @return El estado de portación de arma ("SI" o "NO").
     */
    public String getPortacionArma() {
        return portacionArma;
    }
}
