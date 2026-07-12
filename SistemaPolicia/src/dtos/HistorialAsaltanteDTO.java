package dtos;

import java.util.List;

/**
 * Objeto de transferencia de datos (DTO) que agrupa el historial delictivo de
 * un asaltante, incluyendo su información personal, datos de la banda a la que
 * pertenece y un registro detallado de los delitos cometidos.
 */
public class HistorialAsaltanteDTO {

    /**
     * Identificador único del asaltante.
     */
    private final String idAsaltante;
    /**
     * Nombre completo o alias del asaltante.
     */
    private final String nombreAsaltante;
    /**
     * Información relevante sobre la banda criminal a la que está vinculado.
     */
    private String infoBanda;

    /**
     * Colección de delitos asociados al historial del asaltante.
     */
    private List<DetalleDelitoDTO> listaDeCrimenes;

    /**
     * Construye un nuevo HistorialAsaltanteDTO.
     *
     * * @param idAsaltante Identificador único del sujeto.
     * @param nombreAsaltante Nombre o alias del asaltante.
     * @param infoBanda Datos de la banda criminal vinculada.
     * @param listaDeCrimenes Lista inicial de delitos cometidos.
     */
    public HistorialAsaltanteDTO(String idAsaltante, String nombreAsaltante, String infoBanda, List<DetalleDelitoDTO> listaDeCrimenes) {
        this.idAsaltante = idAsaltante;
        this.nombreAsaltante = nombreAsaltante;
        this.infoBanda = infoBanda;
        this.listaDeCrimenes = listaDeCrimenes;
    }

    /**
     * Agrega un nuevo registro de delito al historial del asaltante.
     *
     * * @param delito El objeto {@link DetalleDelitoDTO} que representa el
     * crimen cometido.
     */
    public void agregarDelito(DetalleDelitoDTO delito) {
        this.listaDeCrimenes.add(delito);
    }

    /**
     * @return El identificador del asaltante.
     */
    public String getIdAsaltante() {
        return idAsaltante;
    }

    /**
     * @return El nombre o alias del asaltante.
     */
    public String getNombreAsaltante() {
        return nombreAsaltante;
    }

    /**
     * @return La información sobre la banda criminal.
     */
    public String getInfoBanda() {
        return infoBanda;
    }

    /**
     * @return La lista de delitos asociados al historial.
     */
    public List<DetalleDelitoDTO> getListaDeCrimenes() {
        return listaDeCrimenes;
    }
}
