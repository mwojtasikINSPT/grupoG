package dtos;

import java.util.List;

/**
 * Objeto de transferencia de datos (DTO) que consolida la información técnica y
 * operativa de una sucursal, incluyendo los detalles de su entidad bancaria,
 * estadísticas de seguridad y el listado de personal de vigilancia asignado.
 */
public class ReporteSucursalDTO {

    /**
     * Código único de la sucursal.
     */
    private final String codigoSucursal;
    /**
     * Dirección física de la sucursal.
     */
    private String domicilioSucursal;
    /**
     * Cantidad total de empleados activos en la sucursal.
     */
    private int cantEmpleados;
    //datos de la entidad a la que pertenece la sucursal
    /**
     * Código de identificación del banco al que pertenece la sucursal.
     */
    private final String codigoBanco;
    /**
     * Domicilio de la casa central de la entidad bancaria.
     */
    private String domicilioCentralBanco;
    /**
     * Contador de asaltos sufridos por la sucursal.
     */
    private int cantAsaltosSufridos;

    /**
     * Lista de vigilantes actualmente asignados a esta sucursal.
     */
    private List<VigilanteAsignadoDTO> listadoVigilantes;

    /**
     * Construye un nuevo ReporteSucursalDTO.
     *
     * @param codigoSucursal Identificador de la sucursal.
     * @param domicilioSucursal Ubicación de la sucursal.
     * @param cantEmpleados Número de personal empleado.
     * @param codigoBanco Identificador de la entidad bancaria.
     * @param domicilioCentralBanco Dirección de la sede central del banco.
     * @param cantAsaltosSufridos Total de incidencias delictivas registradas.
     * @param listadoVigilantes Listado inicial de vigilantes asignados.
     */
    public ReporteSucursalDTO(String codigoSucursal, String domicilioSucursal, int cantEmpleados, String codigoBanco, String domicilioCentralBanco, int cantAsaltosSufridos, List<VigilanteAsignadoDTO> listadoVigilantes) {
        this.codigoSucursal = codigoSucursal;
        this.domicilioSucursal = domicilioSucursal;
        this.cantEmpleados = cantEmpleados;
        this.codigoBanco = codigoBanco;
        this.domicilioCentralBanco = domicilioCentralBanco;
        this.cantAsaltosSufridos = cantAsaltosSufridos;
        this.listadoVigilantes = listadoVigilantes;
    }

    /**
     * Asocia un nuevo vigilante al reporte de la sucursal.
     *
     * @param vigilante Objeto {@link VigilanteAsignadoDTO} con los datos del
     * vigilante.
     */
    public void agregarVigilante(VigilanteAsignadoDTO vigilante) {
        this.listadoVigilantes.add(vigilante);
    }

    //getters
    /**
     * Obtiene código de la sucursal.
     * Obtiene el código de la sucursal.
     *
     * @return El código de la sucursal.
     */
    public String getCodigoSucursal() {
        return codigoSucursal;
    }

    /**
     * Obtiene domicilio
     * Obtiene el domicilio de la sucursal.
     *
     * @return El domicilio de la sucursal.
     */
    public String getDomicilioSucursal() {
        return domicilioSucursal;
    }

    /**
     * Obtiene cant Empleador
     * Obtiene la cantidad de empleados de la sucursal.
     *
     * @return La cantidad de empleados en la sucursal.
     */
    public int getCantEmpleados() {
        return cantEmpleados;
    }

    /**
     * Obtiene codigo
     * Obtiene el código del banco asociado.
     *
     * @return El código del banco asociado.
     */
    public String getCodigoBanco() {
        return codigoBanco;
    }

    /**
     * Obtiene domicilio
     * Obtiene el domicilio central del banco.
     *
     * @return El domicilio de la central bancaria.
     */
    public String getDomicilioCentralBanco() {
        return domicilioCentralBanco;
    }

    /**
     * Obtiene cant Asaltos
     * Obtiene la cantidad total de asaltos sufridos por la sucursal.
     *
     * @return El número total de asaltos registrados.
     */
    public int getCantAsaltosSufridos() {
        return cantAsaltosSufridos;
    }

    /**
     * Genera listado Vigilantes
     * Obtiene el listado de vigilantes asignados a la sucursal.
     *
     * @return La lista de vigilantes asignados.
     */
    public List<VigilanteAsignadoDTO> getListadoVigilantes() {
        return listadoVigilantes;
    }
}
