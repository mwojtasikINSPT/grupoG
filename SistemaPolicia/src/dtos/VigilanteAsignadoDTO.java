package dtos;

/**
 * Objeto de transferencia de datos (DTO) que consolida la información de un
 * vigilante con su estado de portación de armas al momento de ser asignado a
 * una sucursal.
 */
public class VigilanteAsignadoDTO {

    /**
     * Código único de identificación del vigilante.
     */
    private final String codigoVigilante;
    /**
     * Edad actual del vigilante.
     */
    private int edad;
    /**
     * Estado de portación de arma, representado como "SI" o "NO".
     */
    private String portacionArma;

    /**
     * Construye un nuevo VigilanteAsignadoDTO.
     * @param codigoVigilante El identificador del vigilante.
     * @param edad La edad del vigilante.
     * @param portacionArma El estado de portación (ej. "SI" o "NO").
     */
    public VigilanteAsignadoDTO(String codigoVigilante, int edad, String portacionArma) {
        this.codigoVigilante = codigoVigilante;
        this.edad = edad;
        this.portacionArma = portacionArma;
    }

    /**
     * @return El código de identificación del vigilante.
     */
    public String getCodigoVigilante() {
        return codigoVigilante;
    }

    /**
     * @return La edad del vigilante.
     */
    public int getEdad() {
        return edad;
    }

    /**
     * @return El estado de portación de arma ("SI" o "NO").
     */
    public String getPortacionArma() {
        return portacionArma;
    }

}
