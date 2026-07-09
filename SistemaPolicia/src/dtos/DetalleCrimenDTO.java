
//DTO para representar cada delito en particular
package dtos;

public class DetalleCrimenDTO {
    private String idAsalto; //ver si es int o string
    private String fechaAsalto;
    private String detalleSucursal; // Id sucursal - Domicilio
    private String nombreJuez;
    private String veredicto; //ver si dejarlo string o int

    public DetalleCrimenDTO(String idAsalto, String fechaAsalto, String detalleSucursal, String nombreJuez, String veredicto) {
        this.idAsalto = idAsalto;
        this.fechaAsalto = fechaAsalto;
        this.detalleSucursal = detalleSucursal;
        this.nombreJuez = nombreJuez;
        this.veredicto = veredicto;
    }

    public String getIdAsalto() {return idAsalto;}

    public String getFechaAsalto() {return fechaAsalto;}

    public String getDetalleSucursal() {return detalleSucursal;}

    public String getNombreJuez() {return nombreJuez;}

    public String getVeredicto() {return veredicto;} 
}
//ver por que no me deja que sea static