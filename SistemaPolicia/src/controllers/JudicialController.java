package controllers;

import daos.AsaltoDAO;
import daos.CasoJudicialDAO;
import daos.IAsaltoDAO;
import daos.ICasoJudicialDAO;
import daos.IGenericDAO;
import daos.JuezDAO;
import exceptions.ErrorAlGuardarException;
import exceptions.ErrorAlLeerException;
import exceptions.ObjetoNoEncontradoException;
import java.util.List;
import models.CasoJudicial;
import models.Juez;

/**
 * Gestiona jueces, casos judiciales y consultas de condenas.
 *
 * El controlador depende de interfaces de persistencia y utiliza
 * identificadores para representar las relaciones entre los modelos.
 *
 * @author GrupoG
 */
public class JudicialController  {

    private final IGenericDAO<Juez> juezDAO;
    private final ICasoJudicialDAO casoJudicialDAO;
    private final IAsaltoDAO asaltoDAO;

    /**
     * Crea el controlador utilizando la persistencia actual en archivos.
     */
    public JudicialController() {
        this(
                new JuezDAO(),
                new CasoJudicialDAO(),
                new AsaltoDAO()
        );
    }

    /**
     * Crea el controlador manteniendo compatibilidad con la construcción
     * utilizada actualmente por la vista.
     *
     * @param asaltoDAO persistencia utilizada para validar asaltos
     */
    public JudicialController(AsaltoDAO asaltoDAO) {
        this(
                new JuezDAO(),
                new CasoJudicialDAO(),
                asaltoDAO
        );
    }

    /**
     * Crea el controlador manteniendo compatibilidad con el código existente.
     *
     * @param juezDAO persistencia de jueces
     * @param casoJudicialDAO persistencia de casos judiciales
     * @param asaltoDAO persistencia de asaltos
     */
    public JudicialController(
            JuezDAO juezDAO,
            CasoJudicialDAO casoJudicialDAO,
            AsaltoDAO asaltoDAO) {

        this(
                (IGenericDAO<Juez>) juezDAO,
                (ICasoJudicialDAO) casoJudicialDAO,
                (IAsaltoDAO) asaltoDAO
        );
    }

    /**
     * Crea el controlador con las implementaciones de persistencia indicadas.
     *
     * @param juezDAO persistencia utilizada para los jueces
     * @param casoJudicialDAO persistencia utilizada para los casos
     * @param asaltoDAO persistencia utilizada para los asaltos
     * @throws IllegalArgumentException si alguno de los DAO es nulo
     */
    public JudicialController(
           IGenericDAO<Juez> juezDAO,
            ICasoJudicialDAO casoJudicialDAO,
            IAsaltoDAO asaltoDAO) {

        if (juezDAO == null
                || casoJudicialDAO == null
                || asaltoDAO == null) {

            throw new IllegalArgumentException(
                    "Los DAO del módulo judicial son obligatorios."
            );
        }

        this.juezDAO = juezDAO;
        this.casoJudicialDAO = casoJudicialDAO;
        this.asaltoDAO = asaltoDAO;
    }

    /**
     * Registra un juez si su clave todavía no existe.
     *
     * @param claveInterna identificador del juez
     * @param aniosServicio años de servicio
     * @param nombre nombre completo
     * @throws Exception si los datos son inválidos, la clave ya existe o no
     *                   puede guardarse el juez
     */
    public void registrarJuez(
            String claveInterna,
            int aniosServicio,
            String nombre) throws Exception {

        validarDatosJuez(
                claveInterna,
                aniosServicio,
                nombre
        );

        try {
            juezDAO.buscarPorId(claveInterna);

            throw new Exception(
                    "La clave de juez '" + claveInterna
                    + "' ya está en uso."
            );

        } catch (ObjetoNoEncontradoException e) {
            // No encontrarlo indica que la clave está disponible.

        } catch (ErrorAlLeerException e) {
            throw new Exception(
                    "No se pudo verificar el juez: "
                    + e.getMessage()
            );
        }

        try {
            Juez nuevoJuez =
                    new Juez(claveInterna, aniosServicio, nombre);

            juezDAO.guardar(nuevoJuez);

        } catch (ErrorAlGuardarException e) {
            throw new Exception(
                    "Error al guardar el registro del juez: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Registra un caso judicial utilizando los identificadores del asalto y
     * del juez.
     *
     * Antes de guardar comprueba que ambas entidades existan.
     *
     * @param idAsalto identificador del asalto
     * @param claveJuez identificador del juez
     * @param condenado indica si existió una condena
     * @param mesesCarcel duración de la condena
     * @throws Exception si los datos son inválidos, alguna entidad no existe o
     *                   el caso ya está registrado
     */
    public void registrarCasoJudicial(
            String idAsalto,
            String claveJuez,
            boolean condenado,
            int mesesCarcel) throws Exception {

        validarDatosCaso(
                idAsalto,
                claveJuez,
                condenado,
                mesesCarcel
        );

        try {
            // Comprueba que las entidades relacionadas existan.
            asaltoDAO.buscarPorId(idAsalto);
            juezDAO.buscarPorId(claveJuez);

            if (casoJudicialDAO.existe(idAsalto)) {
                throw new Exception(
                        "Ya existe un caso judicial para el asalto: "
                        + idAsalto
                );
            }

            int mesesEfectivos =
                    condenado ? mesesCarcel : 0;

            CasoJudicial nuevoCaso = new CasoJudicial(
                    idAsalto,
                    claveJuez,
                    condenado,
                    mesesEfectivos
            );

            casoJudicialDAO.guardar(nuevoCaso);

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

        } catch (ErrorAlGuardarException e) {
            throw new Exception(
                    "Error al guardar el caso judicial: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Obtiene todos los jueces registrados.
     *
     * @return lista de jueces
     * @throws Exception si no puede accederse a la información
     */
    public List<Juez> listarJueces() throws Exception {
        try {
            return juezDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception(
                    "Error al recuperar la lista de jueces: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Obtiene todos los casos judiciales registrados.
     *
     * @return lista de casos judiciales
     * @throws Exception si no puede accederse a la información
     */
   
    public List<CasoJudicial> listarCasosJudiciales()
            throws Exception {

        try {
            return casoJudicialDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception(
                    "Error al recuperar los casos judiciales: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Obtiene únicamente los casos que terminaron con una condena.
     *
     * @return lista de casos con condena
     * @throws Exception si no puede accederse a la información
     */
    
    public List<CasoJudicial> listarDetenidos()
            throws Exception {

        try {
            List<CasoJudicial> casos =
                    casoJudicialDAO.obtenerTodos();

            if (casos == null) {
                return List.of();
            }

            return casos.stream()
                    .filter(CasoJudicial::isCondenado)
                    .toList();

        } catch (ErrorAlLeerException e) {
            throw new Exception(
                    "Error al recuperar el registro de detenidos: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Valida los datos necesarios para registrar un juez.
     *
     * @param claveInterna identificador del juez
     * @param aniosServicio años de servicio
     * @param nombre nombre completo
     * @throws Exception si algún dato no cumple las reglas
     */
    private void validarDatosJuez(
            String claveInterna,
            int aniosServicio,
            String nombre) throws Exception {

        if (claveInterna == null
                || claveInterna.trim().isEmpty()
                || nombre == null
                || nombre.trim().isEmpty()) {

            throw new Exception(
                    "La clave interna y el nombre del juez "
                    + "son obligatorios."
            );
        }

        if (aniosServicio < 0) {
            throw new Exception(
                    "Los años de servicio no pueden ser negativos."
            );
        }
    }

    /**
     * Valida los datos necesarios para registrar un caso.
     *
     * @param idAsalto identificador del asalto
     * @param claveJuez identificador del juez
     * @param condenado indica si existió una condena
     * @param mesesCarcel duración indicada
     * @throws Exception si algún dato no cumple las reglas
     */
    private void validarDatosCaso(
            String idAsalto,
            String claveJuez,
            boolean condenado,
            int mesesCarcel) throws Exception {

        if (idAsalto == null
                || idAsalto.trim().isEmpty()
                || claveJuez == null
                || claveJuez.trim().isEmpty()) {

            throw new Exception(
                    "El ID del asalto y la clave del juez "
                    + "son obligatorios."
            );
        }

        if (mesesCarcel < 0) {
            throw new Exception(
                    "Los meses de cárcel no pueden ser negativos."
            );
        }

        if (condenado && mesesCarcel <= 0) {
            throw new Exception(
                    "Cuando existe una condena, los meses de cárcel "
                    + "deben ser mayores que cero."
            );
        }
    }
}