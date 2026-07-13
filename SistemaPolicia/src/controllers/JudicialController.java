package controllers;

import daos.AsaltoDAO;
import daos.CasoJudicialDAO;
import daos.JuezDAO;
import exceptions.ErrorAlGuardarException;
import exceptions.ErrorAlLeerException;
import exceptions.ObjetoNoEncontradoException;
import java.util.List;
import models.Asalto;
import models.CasoJudicial;
import models.Juez;

/**
 * Controlador encargado de gestionar la información judicial, incluyendo el
 * registro de jueces, la creación de casos judiciales vinculados a asaltos, y
 * la consulta de sentencias.
 */
public class JudicialController {

    private final JuezDAO juezDAO;
    private final CasoJudicialDAO casoJudicialDAO;
    private final AsaltoDAO asaltoDAO;

    public JudicialController(JuezDAO juezDAO, CasoJudicialDAO casoJudicialDAO, AsaltoDAO asaltoDAO) {
        this.juezDAO = juezDAO;
        this.casoJudicialDAO = casoJudicialDAO;
        this.asaltoDAO = asaltoDAO;
    }
   
    /**
     * Inicializa el controlador de casos judiciales inyectando la dependencia
     * necesaria para la validación de asaltos.
     *
     * @param asaltoDAO Instancia del DAO de asaltos, necesaria para validar que
     * el caso judicial se vincule a un asalto existente.
     */
    public JudicialController(AsaltoDAO asaltoDAO) {
        this.juezDAO = new JuezDAO();
        this.casoJudicialDAO = new CasoJudicialDAO();
        this.asaltoDAO = asaltoDAO;
    }

    /**
     * Registra un nuevo juez en el sistema tras validar que la clave sea única.
     *
     * @param claveInterna ID único del juez.
     * @param aniosServicio Años de trayectoria del magistrado.
     * @param nombre Nombre completo del juez.
     * @throws Exception Si los datos son inválidos o la clave ya existe.
     */
    public void registrarJuez(String claveInterna, int aniosServicio, String nombre) throws Exception {

        if (claveInterna.trim().isEmpty() || nombre.trim().isEmpty()) {
            throw new Exception("La clave interna y el nombre del juez no pueden estar vacíos.");
        }
        if (aniosServicio < 0) {
            throw new Exception("Los años de servicio no pueden ser negativos.");
        }
        try {
            //Validar que no haya clave interna duplicada
            try {
                juezDAO.buscarPorId(claveInterna);
                throw new Exception("La clave de juez '" + claveInterna + "' ya está en uso.");
            } catch (ObjetoNoEncontradoException e) {
                //Si no lo encuentra, se puede usar.
            }
            //Crear juez
            Juez nuevoJuez = new Juez(claveInterna, aniosServicio, nombre);
            juezDAO.guardar(nuevoJuez);
        } catch (ErrorAlGuardarException e) {
            throw new Exception("Error al guardar el registro del juez: " + e.getMessage());
        }
    }

    /**
     * Registra un caso judicial vinculado a un asalto existente.
     *
     * @param idAsalto ID del asalto que motiva el caso.
     * @param claveJuez Clave del juez asignado.
     * @param condenado Indica si hubo sentencia condenatoria.
     * @param mesesCarcel Meses de prisión impuestos.
     * @throws Exception Si el asalto o juez no existen, o si el caso ya está
     * registrado.
     */
    public void registrarCasoJudicial(String idAsalto, String claveJuez, boolean condenado, int mesesCarcel) throws Exception {
        if (idAsalto.trim().isEmpty() || claveJuez.trim().isEmpty()) {
            throw new Exception("El ID del asalto y la clave del juez son campos obligatorios.");
        }

        //Si no esta condenado,  meses en la carcel= 0
        int mesesEfectivos = condenado ? mesesCarcel : 0;

        if (condenado && mesesEfectivos <= 0) {
            throw new Exception("Si el veredicto es CONDENADO, los meses en la cárcel deben ser mayores a 0.");
        }

        try {
            // Valido existencia de entidades
            Asalto asaltoReal = asaltoDAO.buscarPorId(idAsalto);
            Juez juezReal = juezDAO.buscarPorId(claveJuez);

            //Verifico que no exista un caso ya registrado para este asalto
            try {
                casoJudicialDAO.buscarPorId(idAsalto);
                throw new Exception("Ya existe un caso judicial registrado para el asalto: " + idAsalto);
            } catch (ObjetoNoEncontradoException e) {
                // El asalto no tiene caso, procedemos
            }

            CasoJudicial casoJucidial = new CasoJudicial(asaltoReal, juezReal, condenado, mesesEfectivos);
            // Guardo el caso en el archivo usando su DAO
            casoJudicialDAO.guardar(casoJucidial);

        } catch (ObjetoNoEncontradoException e) {
            throw new Exception("Entidad no encontrada: " + e.getMessage());
        } catch (ErrorAlGuardarException e) {
            throw new Exception("Error al guardar el caso judicial: " + e.getMessage());
        }
    }

    /**
     * Obtiene el listado de todos los jueces registrados.
     *
     * @return Lista de objetos {@link Juez}.
     * @throws Exception Si hay error en la lectura.
     */
    public List<Juez> listarJueces() throws Exception {
        try {
            return juezDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception("Error al recuperar la lista de jueces: " + e.getMessage());
        }
    }

    /**
     * Obtiene el listado de todos los casos judiciales.
     *
     * @return Lista de objetos {@link CasoJudicial}.
     * @throws Exception Si hay error en la lectura.
     */
    public List<CasoJudicial> listarCasosJudiciales() throws Exception {
        try {
            return casoJudicialDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception("Error al recuperar la lista de casos judiciales: " + e.getMessage());
        }
    }

    /**
     * Filtra y obtiene solo los casos que poseen una condena (detenidos).
     *
     * @return Lista de {@link CasoJudicial} con condena.
     * @throws Exception Si hay error en la lectura.
     */
    public List<CasoJudicial> listarDetenidos() throws Exception {
        try {
            List<CasoJudicial> todosLosCasos = casoJudicialDAO.obtenerTodos();

            // Validación de seguridad para evitar errores si la lista es nula
            if (todosLosCasos == null) {
                return List.of(); // Retorna una lista vacía en lugar de romper el programa
            }

            // Me quedo solo con los condenados
            return todosLosCasos.stream()
                    .filter(CasoJudicial::isCondenado)
                    .toList();

        } catch (ErrorAlLeerException e) {
            throw new Exception("Error al recuperar el registro de detenidos: " + e.getMessage());
        }
    }
}
