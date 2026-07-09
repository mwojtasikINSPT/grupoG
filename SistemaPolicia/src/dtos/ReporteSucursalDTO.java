package dtos;

// DTO para consolidar información de una sucursal (vigilantes asignados y asaltos sufridos).

import java.util.ArrayList;
import java.util.List;

public class ReporteSucursalDTO {
    private String codigoSucursal; //ver si es int o string
    private String domicilioSucursal;
    private int cantEmpleados;
    
    //datos de la entidad a la que pertenece la sucursal
    private String codigoBanco;
    private String domicilioCentralBanco;
    
    private int cantAsaltosSufridos;
    
    private List <VigilanteAsignadoDTO> listadoVigilantes;  

    public ReporteSucursalDTO(String codigoSucursal, String domicilioSucursal, int cantEmpleados, String codigoBanco, String domicilioCentralBanco, int cantAsaltosSufridos, List<VigilanteAsignadoDTO> listadoVigilantes) {
        this.codigoSucursal = codigoSucursal;
        this.domicilioSucursal = domicilioSucursal;
        this.cantEmpleados = cantEmpleados;
        this.codigoBanco = codigoBanco;
        this.domicilioCentralBanco = domicilioCentralBanco;
        this.cantAsaltosSufridos = cantAsaltosSufridos;
        this.listadoVigilantes = new ArrayList<>();
    }
    
    //agregar vigilante a la sucursal
    public void agregarVigilante(VigilanteAsignadoDTO vigilante){
        this.listadoVigilantes.add(vigilante);
    }
    
    //getters

    public String getCodigoSucursal() {return codigoSucursal;}

    public String getDomicilioSucursal() {return domicilioSucursal;}

    public int getCantEmpleados() {return cantEmpleados;}

    public String getCodigoBanco() {return codigoBanco;}

    public String getDomicilioCentralBanco() {return domicilioCentralBanco;}

    public int getCantAsaltosSufridos() {return cantAsaltosSufridos;}

    public List<VigilanteAsignadoDTO> getListadoVigilantes() {return listadoVigilantes;}
}

