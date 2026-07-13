package controllers;

import daos.AsaltanteDAO;
import daos.AsaltoDAO;
import daos.BandaDAO;
import daos.SucursalDAO;
import exceptions.ErrorAlGuardarException;
import exceptions.ErrorAlLeerException;
import exceptions.ObjetoNoEncontradoException;
import java.time.LocalDate;
import java.util.List;
import models.Asaltante;
import models.Asalto;
import models.Banda;
import models.Sucursal;

/**
 * Controlador encargado de gestionar las operaciones relacionadas con asaltos,
 * bandas y asaltantes, actuando como intermediario entre la capa de
 * persistencia (DAO) y la lógica de negocio.
 */
public class AsaltosController {

    private final BandaDAO bandaDAO;
    private final AsaltanteDAO asaltanteDAO;
    private final AsaltoDAO asaltoDAO;
    private final SucursalDAO sucursalDAO;

    /**
     * Constructor del controlador de asaltos.
     *
     * Inyecta las dependencias necesarias para gestionar la lógica de asaltos:
     * - BandaDAO: acceso a datos de bandas. - AsaltanteDAO: acceso a datos de
     * asaltantes. - SucursalDAO: acceso a datos de sucursales.
     *
     * Además, inicializa el AsaltoDAO utilizando AsaltanteDAO y SucursalDAO, ya
     * que son requeridos para la gestión de los asaltos.
     *
     * @param bandaDAO DAO para operaciones sobre bandas
     * @param asaltanteDAO DAO para operaciones sobre asaltantes
     * @param sucursalDAO DAO para operaciones sobre sucursales
     */
    public AsaltosController(BandaDAO bandaDAO, AsaltanteDAO asaltanteDAO, SucursalDAO sucursalDAO) {
        this.bandaDAO = bandaDAO;
        this.asaltanteDAO = asaltanteDAO;
        this.sucursalDAO = sucursalDAO;
        // inyecto DAOs necesarios al AsaltoDAO
        this.asaltoDAO = new AsaltoDAO(asaltanteDAO, sucursalDAO);
    }

    /**
     * Registra una nueva banda criminal en el sistema tras validar los datos.
     *
     * @param numeroBanda Identificador único de la banda.
     * @param cantMiembros Cantidad de integrantes de la banda.
     * @throws Exception Si la banda ya existe o los datos son inválidos.
     */
    public void registrarBanda(String numeroBanda, int cantMiembros) throws Exception {
        if (numeroBanda.trim().isEmpty()) {
            throw new Exception("El numero de banda no puede estar vacío.");
        }

        if (cantMiembros < 0) {
            throw new Exception("La cantidad de miembros no puede ser negativa.");
        }

        try {
            // Verificamos si existe antes de intentar guardar
            boolean existe = true;
            try {
                bandaDAO.buscarPorId(numeroBanda);
            } catch (ObjetoNoEncontradoException e) {
                existe = false;
            }

            if (existe) {
                throw new Exception("La banda número '" + numeroBanda + "' ya está registrada.");
            }

            Banda nuevaBanda = new Banda(numeroBanda, cantMiembros);
            bandaDAO.guardar(nuevaBanda);
        } catch (ErrorAlGuardarException e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Registra un nuevo asaltante vinculándolo a una banda ya existente.
     *
     * @param clave Identificador único del asaltante.
     * @param nombreCompleto Nombre del asaltante.
     * @param numeroBanda Código de la banda a la cual se asigna.
     * @throws Exception Si la banda no existe o la clave ya está en uso.
     */
    public void registrarAsaltante(String clave, String nombreCompleto, String numeroBanda) throws Exception {
        try {
            // Verificar que la banda existe
            Banda banda = bandaDAO.buscarPorId(numeroBanda);

            // Verificar que la clave del asaltante no este duplicado
            boolean existe = true;
            try {
                asaltanteDAO.buscarPorId(clave);
            } catch (ObjetoNoEncontradoException e) {
                existe = false;
            }

            if (existe) {
                throw new Exception("La clave del asaltante '" + clave + "' ya se encuentra en uso.");
            }

            //Armar el asaltante asociando la banda
            Asaltante nuevoAsaltante = new Asaltante(clave, nombreCompleto, banda);
            asaltanteDAO.guardar(nuevoAsaltante);

        } catch (ErrorAlGuardarException e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Registra un nuevo asalto en el sistema validando la existencia de los
     * involucrados.
     *
     * @param idAsalto ID único del asalto.
     * @param claveAsaltante ID del asaltante involucrado.
     * @param codigoSucursal ID de la sucursal afectada.
     * @param fechaStr Fecha del asalto en formato YYYY-MM-DD.
     * @throws Exception Si los IDs no existen, el ID de asalto está duplicado o
     * la fecha es inválida.
     */
    public void registrarAsalto(String idAsalto, String claveAsaltante, String codigoSucursal, String fechaStr) throws Exception {
        try {
            // Verificar que el id del asalto no se duplique.
            boolean existe = true;
            try {
                asaltoDAO.buscarPorId(idAsalto);
            } catch (ObjetoNoEncontradoException e) {
                existe = false;
            }
            if (existe) {
                throw new Exception("El id de asalto '" + idAsalto + "' ya está en uso.");
            }
            // Verificar que el asaltante existe
            Asaltante asaltante = asaltanteDAO.buscarPorId(claveAsaltante);

            // Verificar que la sucursal existe
            Sucursal sucursal = sucursalDAO.buscarPorId(codigoSucursal);

            //Fecha
            LocalDate fecha = LocalDate.parse(fechaStr);

            //Armar asalto
            Asalto nuevoAsalto = new Asalto(idAsalto, asaltante, sucursal, fecha);
            asaltoDAO.guardar(nuevoAsalto);

        } catch (ObjetoNoEncontradoException e) {
            throw new Exception("Entidad no encontrada: " + e.getMessage());
        } catch (java.time.format.DateTimeParseException e) {
            throw new Exception("Formato de fecha inválido. Por favor use el formato  YYYY-MM-DD.");
        } catch (ErrorAlGuardarException e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Recupera el listado completo de asaltos registrados.
     *
     * @return Lista de objetos {@link Asalto}.
     * @throws Exception Si ocurre un error durante la lectura.
     */
    public List<Asalto> listarAsaltos() throws Exception {
        try {
            return asaltoDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Recupera el listado completo de bandas registradas.
     *
     * @return Lista de objetos {@link Banda}.
     * @throws Exception Si ocurre un error durante la lectura.
     */
    public List<Banda> listarBandas() throws Exception {
        try {
            return bandaDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception(e.getMessage());
        }
    }
}
