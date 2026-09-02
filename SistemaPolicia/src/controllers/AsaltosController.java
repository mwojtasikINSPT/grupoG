package controllers;

import daos.AsaltanteDAO;
import daos.AsaltoDAO;
import daos.BandaDAO;
import daos.IAsaltoDAO;
import daos.IGenericDAO;
import daos.SucursalDAO;
import exceptions.ErrorAlGuardarException;
import exceptions.ErrorAlLeerException;
import exceptions.ObjetoNoEncontradoException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import models.Asaltante;
import models.Asalto;
import models.Banda;
import models.Sucursal;

/**
 * Gestiona bandas, asaltantes y asaltos.
 * Depende de interfaces de persistencia para permitir diferentes
 * formas de almacenamiento.
 *
 * @author GrupoG
 */
public class AsaltosController {

    private final IGenericDAO<Banda> bandaDAO;
    private final IGenericDAO<Asaltante> asaltanteDAO;
    private final IAsaltoDAO asaltoDAO;
    private final IGenericDAO<Sucursal> sucursalDAO;

    /**
     * Crea el controlador con los DAO utilizados por la aplicación.
     */
    public AsaltosController() {
        this(
                new BandaDAO(),
                new AsaltanteDAO(),
                new AsaltoDAO(),
                new SucursalDAO()
        );
    }

    /**
     * Mantiene compatibilidad con la construcción utilizada por las vistas.
     *
     * @param bandaDAO persistencia de bandas
     * @param asaltanteDAO persistencia de asaltantes
     * @param sucursalDAO persistencia de sucursales
     */
    public AsaltosController(
            BandaDAO bandaDAO,
            AsaltanteDAO asaltanteDAO,
            SucursalDAO sucursalDAO) {

        this(
                bandaDAO,
                asaltanteDAO,
                new AsaltoDAO(),
                sucursalDAO
        );
    }

    /**
     * Crea el controlador con las implementaciones recibidas.
     *
     * @param bandaDAO persistencia de bandas
     * @param asaltanteDAO persistencia de asaltantes
     * @param asaltoDAO persistencia de asaltos
     * @param sucursalDAO persistencia de sucursales
     * @throws IllegalArgumentException si algún DAO es nulo
     */
    public AsaltosController(
            IGenericDAO<Banda> bandaDAO,
            IGenericDAO<Asaltante> asaltanteDAO,
            IAsaltoDAO asaltoDAO,
            IGenericDAO<Sucursal> sucursalDAO) {

        if (bandaDAO == null
                || asaltanteDAO == null
                || asaltoDAO == null
                || sucursalDAO == null) {

            throw new IllegalArgumentException(
                    "Los DAO del módulo de asaltos son obligatorios."
            );
        }

        this.bandaDAO = bandaDAO;
        this.asaltanteDAO = asaltanteDAO;
        this.asaltoDAO = asaltoDAO;
        this.sucursalDAO = sucursalDAO;
    }

    /**
     * Registra una banda si su identificador está disponible.
     *
     * @param numeroBanda identificador de la banda
     * @param cantMiembros cantidad de integrantes
     * @throws Exception si los datos son inválidos o no puede guardarse
     */
    public void registrarBanda(
            String numeroBanda,
            int cantMiembros) throws Exception {

        if (numeroBanda == null
                || numeroBanda.trim().isEmpty()) {

            throw new Exception(
                    "El número de banda no puede estar vacío."
            );
        }

        if (cantMiembros < 0) {
            throw new Exception(
                    "La cantidad de miembros no puede ser negativa."
            );
        }

        try {
            bandaDAO.buscarPorId(numeroBanda);

            throw new Exception(
                    "La banda número '" + numeroBanda
                    + "' ya está registrada."
            );

        } catch (ObjetoNoEncontradoException e) {
            // No encontrarla indica que el número está disponible.

        } catch (ErrorAlLeerException e) {
            throw new Exception(
                    "No se pudo verificar la banda: "
                    + e.getMessage()
            );
        }

        try {
            Banda nuevaBanda =
                    new Banda(numeroBanda, cantMiembros);

            bandaDAO.guardar(nuevaBanda);

        } catch (ErrorAlGuardarException e) {
            throw new Exception(
                    "No se pudo guardar la banda: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Registra un asaltante vinculado con una banda existente.
     *
     * @param clave identificador del asaltante
     * @param nombreCompleto nombre completo
     * @param numeroBanda identificador de la banda
     * @throws Exception si los datos son inválidos o no puede guardarse
     */
    public void registrarAsaltante(
            String clave,
            String nombreCompleto,
            String numeroBanda) throws Exception {

        if (clave == null
                || clave.trim().isEmpty()
                || nombreCompleto == null
                || nombreCompleto.trim().isEmpty()
                || numeroBanda == null
                || numeroBanda.trim().isEmpty()) {

            throw new Exception(
                    "La clave, el nombre y la banda son obligatorios."
            );
        }

        try {
            // Comprueba que la banda relacionada exista.
            bandaDAO.buscarPorId(numeroBanda);

            verificarClaveAsaltanteDisponible(clave);

            Asaltante nuevoAsaltante = new Asaltante(
                    clave,
                    nombreCompleto,
                    numeroBanda
            );

            asaltanteDAO.guardar(nuevoAsaltante);

        } catch (ObjetoNoEncontradoException e) {
            throw new Exception(
                    "La banda indicada no existe."
            );

        } catch (ErrorAlLeerException e) {
            throw new Exception(
                    "No se pudo consultar la información: "
                    + e.getMessage()
            );

        } catch (ErrorAlGuardarException e) {
            throw new Exception(
                    "No se pudo guardar el asaltante: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Registra un asalto con los identificadores relacionados.
     *
     * @param idAsalto identificador del asalto
     * @param claveAsaltante identificador del asaltante
     * @param codigoSucursal identificador de la sucursal
     * @param fechaStr fecha con formato {@code YYYY-MM-DD}
     * @throws Exception si los datos son inválidos o no puede guardarse
     */
    public void registrarAsalto(
            String idAsalto,
            String claveAsaltante,
            String codigoSucursal,
            String fechaStr) throws Exception {

        if (idAsalto == null
                || idAsalto.trim().isEmpty()
                || claveAsaltante == null
                || claveAsaltante.trim().isEmpty()
                || codigoSucursal == null
                || codigoSucursal.trim().isEmpty()
                || fechaStr == null
                || fechaStr.trim().isEmpty()) {

            throw new Exception(
                    "Los datos del asalto son obligatorios."
            );
        }

        if (asaltoDAO.existe(idAsalto)) {
            throw new Exception(
                    "El ID de asalto '" + idAsalto
                    + "' ya está en uso."
            );
        }

        try {
            // Comprueba que las entidades relacionadas existan.
            asaltanteDAO.buscarPorId(claveAsaltante);
            sucursalDAO.buscarPorId(codigoSucursal);

            LocalDate fecha = LocalDate.parse(fechaStr);

            Asalto nuevoAsalto = new Asalto(
                    idAsalto,
                    claveAsaltante,
                    codigoSucursal,
                    fecha
            );

            asaltoDAO.guardar(nuevoAsalto);

        } catch (ObjetoNoEncontradoException e) {
            throw new Exception(
                    "Entidad relacionada no encontrada: "
                    + e.getMessage()
            );

        } catch (ErrorAlLeerException e) {
            throw new Exception(
                    "No se pudo validar la información relacionada: "
                    + e.getMessage()
            );

        } catch (DateTimeParseException e) {
            throw new Exception(
                    "Formato de fecha inválido. Utilice YYYY-MM-DD."
            );

        } catch (ErrorAlGuardarException e) {
            throw new Exception(
                    "No se pudo guardar el asalto: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Obtiene los asaltos registrados.
     *
     * @return lista de asaltos
     * @throws Exception si no puede accederse a la información
     */
    public List<Asalto> listarAsaltos() throws Exception {
        try {
            return asaltoDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception(
                    "No se pudieron recuperar los asaltos: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Obtiene las bandas registradas.
     *
     * @return lista de bandas
     * @throws Exception si no puede accederse a la información
     */
    public List<Banda> listarBandas() throws Exception {
        try {
            return bandaDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception(
                    "No se pudieron recuperar las bandas: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Comprueba que una clave de asaltante esté disponible.
     *
     * @param clave clave que se verificará
     * @throws Exception si la clave ya está utilizada
     * @throws ErrorAlLeerException si falla la consulta
     */
    private void verificarClaveAsaltanteDisponible(String clave)
            throws Exception, ErrorAlLeerException {

        try {
            asaltanteDAO.buscarPorId(clave);

            throw new Exception(
                    "La clave del asaltante '" + clave
                    + "' ya se encuentra en uso."
            );

        } catch (ObjetoNoEncontradoException e) {
            // No encontrarlo indica que la clave está disponible.
        }
    }
}