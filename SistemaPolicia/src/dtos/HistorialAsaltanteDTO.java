package dtos;

// DTO para agrupar los asaltos, bandas y condenas de un asaltante.

import java.util.List;

public class HistorialAsaltanteDTO {
    private String idAsaltante;
    private String nombreAsaltante;
    private String infoBanda;
    
    private List<DetalleCrimenDTO> listaDeCrimenes;

    public HistorialAsaltanteDTO(String idAsaltante, String nombreAsaltante, String infoBanda, List<DetalleCrimenDTO> listaDeCrimenes) {
        this.idAsaltante = idAsaltante;
        this.nombreAsaltante = nombreAsaltante;
        this.infoBanda = infoBanda;
        this.listaDeCrimenes = listaDeCrimenes;
    }

    //Agregar delitos que cometió
    public void agregarDelito(DetalleCrimenDTO delito){
        this.listaDeCrimenes.add(delito);
    }

    public String getIdAsaltante() {return idAsaltante;}

    public String getNombreAsaltante() {return nombreAsaltante;}

    public String getInfoBanda() {return infoBanda;}

    public List<DetalleCrimenDTO> getListaDeCrimenes() {return listaDeCrimenes;}    
}
