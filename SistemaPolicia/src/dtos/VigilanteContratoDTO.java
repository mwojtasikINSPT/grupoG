package dtos;

// DTO para mostrar al Vigilante solo los datos que le interesan (sus fechas y sucursales).
public class VigilanteContratoDTO {

    private String codigoVigilante;
    private int edadVigilante;
    private String codigoSucursal;
    private String domicilioSucursal;
    private String fecha;           //String para que muestre mejor la fecha 
    private String portacionArma;   //String para que muestre SI / NO

    public VigilanteContratoDTO(String codigoVigilante, int edadVigilante, String codigoSucursal, String domicilioSucursal, String fecha, String portacionArma) {
        this.codigoVigilante = codigoVigilante;
        this.edadVigilante = edadVigilante;
        this.codigoSucursal = codigoSucursal;
        this.domicilioSucursal = domicilioSucursal;
        this.fecha = fecha;
        this.portacionArma = portacionArma;
    }

    public String getCodigoVigilante() {return codigoVigilante;}

    public int getEdadVigilante() {return edadVigilante;}

    public String getCodigoSucursal() {return codigoSucursal;}

    public String getDomicilioSucursal() {return domicilioSucursal;}

    public String getFecha() {return fecha;}

    public String getPortacionArma() {return portacionArma;}    
}
