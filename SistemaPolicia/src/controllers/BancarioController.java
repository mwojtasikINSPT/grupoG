package controllers;

import daos.ContratoVigilanciaDAO;
import daos.EntidadBancariaDAO;
import daos.SucursalDAO;
import daos.VigilanteDAO;
import exceptions.ErrorAlEliminarException;
import exceptions.ErrorAlGuardarException;
import exceptions.ErrorAlLeerException;
import exceptions.ObjetoNoEncontradoException;
import java.time.LocalDate;
import java.util.List;
import models.ContratoVigilancia;
import models.EntidadBancaria;
import models.Sucursal;
import models.Vigilante;

/**
 * Controlador encargado de gestionar las operaciones bancarias, incluyendo el
 * registro y listado de vigilantes, sucursales y contratos de vigilancia.
 */
public class BancarioController {

    private final SucursalDAO sucursalDAO;
    private final VigilanteDAO vigilanteDAO;
    private final ContratoVigilanciaDAO contratoDAO;
    private final EntidadBancariaDAO entidadBancariaDAO;

    /**
     * Constructor que inicializa las instancias de los DAOs necesarios para la
     * gestión bancaria.
     */
    public BancarioController() {
        this.sucursalDAO = new SucursalDAO();
        this.vigilanteDAO = new VigilanteDAO();
        this.contratoDAO = new ContratoVigilanciaDAO();
        this.entidadBancariaDAO = new EntidadBancariaDAO();
    }

    /**
     * Registra un nuevo vigilante en el sistema realizando validaciones de
     * datos.
     *
     * @param codigo Código identificador del vigilante.
     * @param edad   Edad del vigilante.
     * @throws Exception Si el código está vacío, el vigilante es menor de edad
     *                   o ya existe.
     */
    public void registrarVigilante(String codigo, int edad) throws Exception {
        if (codigo.trim().isEmpty()) {
            throw new Exception("El código del vigilante no puede estar vacío.");
        }

        if (edad < 18) {
            throw new Exception("El vigilante debe ser mayor de edad (mínimo 18 años.)");
        }

        try {
            try {
                vigilanteDAO.buscarPorId(codigo);
                throw new Exception("El código de vigilante '" + codigo + "' ya está registrado.");
            } catch (ObjetoNoEncontradoException e) {
                // Si no lo encuentra, se puede crear
            }

            Vigilante nuevoVigilante = new Vigilante(codigo, edad);
            vigilanteDAO.guardar(nuevoVigilante);
        } catch (ErrorAlGuardarException e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Registra una nueva entidad bancaria en el sistema.
     * <p>
     * El método valida que los campos requeridos no estén vacíos. Luego, verifica
     * que no exista otra entidad con el mismo código antes de proceder a guardarla.
     * </p>
     *
     * @param codigo           El código único que identifica a la entidad bancaria.
     *                         No debe estar vacío.
     * @param domicilioCentral La dirección de la sede central de la entidad. No
     *                         debe estar vacía.
     * @throws Exception Si el código o el domicilio están vacíos, o si la entidad
     *                   bancaria ya se encuentra registrada.
     */
    public void registrarEntidadBancaria(String codigo, String domicilioCentral) throws Exception {
        if (codigo.trim().isEmpty() || domicilioCentral.trim().isEmpty()) {
            throw new Exception("El código y el domicilio central son obligatorios.");
        }

        try {
            entidadBancariaDAO.buscarPorId(codigo);
            throw new Exception("La entidad bancaria ya existe.");
        } catch (ObjetoNoEncontradoException e) {
            EntidadBancaria entidad = new EntidadBancaria(codigo, domicilioCentral);
            entidadBancariaDAO.guardar(entidad);
        }
    }

    /**
     * Registra una nueva sucursal vinculada a una entidad bancaria.
     *
     * @param codigoSucursal Código identificador de la sucursal.
     * @param domicilio      Domicilio físico de la sucursal.
     * @param numEmpleado    Cantidad de empleados.
     * @param codigoBanco    Código de la entidad bancaria asociada.
     * @throws Exception Si los códigos están vacíos o la sucursal ya existe.
     */
    public void registrarSucursal(String codigoSucursal, String domicilio, int numEmpleado, String codigoBanco)
            throws Exception {
        if (codigoSucursal.trim().isEmpty() || codigoBanco.trim().isEmpty()) {
            throw new Exception("El código de sucursal y el código de banco son obligatorios.");
        }
        try {
            try {
                sucursalDAO.buscarPorId(codigoSucursal);
                throw new Exception("La sucursal '" + codigoSucursal + "' ya existe.");
            } catch (ObjetoNoEncontradoException e) {
            }
            // Si el banco no existe, la Suc no se guarda. Si existe, queda asociada al
            // banco con su dom central
            EntidadBancaria bancoAsociado = entidadBancariaDAO.buscarPorId(codigoBanco);
            Sucursal nuevaSucursal = new Sucursal(codigoSucursal, domicilio, numEmpleado, bancoAsociado);

            sucursalDAO.guardar(nuevaSucursal);
        } catch (ErrorAlGuardarException e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Registra un contrato de vigilancia vinculando una sucursal, un vigilante
     * y una fecha.
     *
     * @param codigoSucursal   Código de la sucursal.
     * @param codigoVigilancia Código identificador único del contrato.
     * @param codigoVigilante  Código del vigilante a contratar.
     * @param fechaStr         Fecha de inicio en formato YYYY-MM-DD.
     * @param conArma          Indica si el vigilante porta arma.
     * @throws Exception Si la sucursal o vigilante no existen, el contrato está
     *                   duplicado o la fecha es inválida.
     */
    public void registrarContratoVigilancia(String codigoSucursal, String codigoVigilancia, String codigoVigilante,
            String fechaStr, boolean conArma) throws Exception {
        try {
            // Verificar que la sucursal existe
            Sucursal sucursal = sucursalDAO.buscarPorId(codigoSucursal);

            // Verificar que el vigilante existe
            Vigilante vigilante = vigilanteDAO.buscarPorId(codigoVigilante);

            try {
                contratoDAO.buscarPorId(codigoVigilancia);
                throw new Exception("El contrato '" + codigoVigilancia + "' ya está registrado.");
            } catch (ObjetoNoEncontradoException e) {
                // Si no existe, podemos continuar con la creación
            }

            // Fecha
            LocalDate fecha = LocalDate.parse(fechaStr);

            // Armar contrato
            ContratoVigilancia nuevoContrato = new ContratoVigilancia(sucursal, vigilante, fecha, conArma);
            contratoDAO.guardar(nuevoContrato);

        } catch (ObjetoNoEncontradoException e) {
            throw new Exception("Entidad no encontrada: " + e.getMessage());
        } catch (java.time.format.DateTimeParseException e) {
            throw new Exception("Formato de fecha inválido. Por favor use el formato YYYY-MM-DD.");
        } catch (ErrorAlGuardarException e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Recupera el listado de todos los contratos de vigilancia.
     *
     * @return Lista de {@link ContratoVigilancia}.
     * @throws Exception Si ocurre un error de lectura.
     */
    public List<ContratoVigilancia> listarContratosVigilancia() throws Exception {
        try {
            return contratoDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Recupera el listado de vigilantes registrados.
     *
     * @return Lista de {@link Vigilante}.
     * @throws Exception Si ocurre un error de lectura.
     */
    public List<Vigilante> listarVigilantes() throws Exception {
        try {
            return vigilanteDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception("Error al recuperar la lista de vigilantes: " + e.getMessage());
        }
    }

    /**
     * Recupera el listado de todas las sucursales bancarias.
     *
     * @return Lista de {@link Sucursal}.
     * @throws Exception Si ocurre un error de lectura.
     */
    public List<Sucursal> listarSucursales() throws Exception {
        try {
            return sucursalDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception("Error al recuperar la lista de sucursales: " + e.getMessage());
        }
    }

    /**
     * Busca un vigilante por su código identificador.
     *
     * @param codigo Código del vigilante.
     * @return El objeto {@link Vigilante} encontrado.
     * @throws Exception Si el vigilante no existe o hay error de lectura.
     */
    public Vigilante buscarVigilantePorId(final String codigo) throws Exception {
        try {
            return vigilanteDAO.buscarPorId(codigo);
        } catch (ObjetoNoEncontradoException e) {
            throw new Exception("No se encontró el vigilante con código: " + codigo);
        } catch (ErrorAlLeerException e) {
            throw new Exception("Error al acceder a los datos: " + e.getMessage());
        }
    }

    /**
     * Recupera la lista completa de todas las entidades bancarias registradas en el
     * sistema.
     * <p>
     * Este método consulta la capa de persistencia a través del DAO. Si ocurre un
     * error durante la lectura de los datos, captura la excepción interna y la
     * relanza
     * con un mensaje más descriptivo.
     * </p>
     *
     * @return Una lista de objetos {@link EntidadBancaria} con todas las entidades
     *         encontradas.
     *         Si no hay registros, la lista podría estar vacía.
     * @throws Exception Si ocurre un problema de lectura en la base de datos o en
     *                   el almacenamiento persistente.
     */
    public List<EntidadBancaria> listarEntidadesBancarias() throws Exception {
        try {
            return entidadBancariaDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception("Error al recuperar la lista de entidades bancarias: " + e.getMessage());
        }
    }

    /**
     * Elimina un vigilante del sistema según su código, siempre y cuando no tenga
     * contratos de vigilancia asociados.
     * <p>
     * El proceso realiza las siguientes validaciones antes de la eliminación:
     * <ul>
     * <li>Verifica que el código proporcionado no sea nulo ni esté vacío.</li>
     * <li>Comprueba la existencia del vigilante en la base de datos.</li>
     * <li>Valida que el vigilante no esté vinculado a ningún contrato activo o
     * registrado.</li>
     * </ul>
     * </p>
     *
     * @param codigo El código único que identifica al vigilante que se desea
     *               eliminar.
     *               No debe ser {@colis null} ni una cadena vacía.
     * @throws Exception Si el código es inválido, si el vigilante no existe, si el
     *                   vigilante
     *                   tiene contratos asociados, o si ocurre un error interno en
     *                   la base de datos.
     */
    public void eliminarVigilante(String codigo) throws Exception {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new Exception("El código del vigilante es obligatorio.");
        }

        try {
            vigilanteDAO.buscarPorId(codigo);

            List<ContratoVigilancia> contratos = contratoDAO.obtenerTodos();

            boolean tieneContratos = contratos.stream()
                    .anyMatch(contrato -> contrato.getVigilante().getCodigo().equals(codigo));

            if (tieneContratos) {
                throw new Exception(
                        "No se puede eliminar el vigilante porque tiene contratos de vigilancia asociados.");
            }

            vigilanteDAO.eliminar(codigo);

        } catch (ObjetoNoEncontradoException e) {
            throw new Exception("No existe un vigilante con el código: " + codigo);
        } catch (ErrorAlLeerException e) {
            throw new Exception("No se pudieron verificar los contratos: " + e.getMessage());
        } catch (ErrorAlEliminarException e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Elimina una sucursal del sistema siempre que no tenga contratos de
     * vigilancia asociados.
     *
     * @param codigo Código de la sucursal a eliminar.
     * @throws Exception Si el código es inválido, la sucursal no existe, tiene
     *                   contratos asociados o falla la eliminación.
     */
    public void eliminarSucursal(String codigo) throws Exception {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new Exception("El código de la sucursal es obligatorio.");
        }

        try {
            sucursalDAO.buscarPorId(codigo);

            List<ContratoVigilancia> contratos = contratoDAO.obtenerTodos();
            boolean tieneContratos = contratos.stream()
                    .anyMatch(contrato -> contrato.getSucursal().getCodigo().equals(codigo));

            if (tieneContratos) {
                throw new Exception("No se puede eliminar la sucursal porque tiene contratos de vigilancia asociados.");
            }

            sucursalDAO.eliminar(codigo);
        } catch (ObjetoNoEncontradoException e) {
            throw new Exception("No existe una sucursal con el código: " + codigo);
        } catch (ErrorAlLeerException e) {
            throw new Exception("No se pudieron verificar los contratos: " + e.getMessage());
        } catch (ErrorAlEliminarException e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Elimina un contrato de vigilancia usando su clave compuesta:
     * sucursal, vigilante y fecha.
     *
     * @param codigoSucursal Código de la sucursal.
     * @param codigoVigilante Código del vigilante.
     * @param fechaStr Fecha del contrato en formato YYYY-MM-DD.
     * @throws Exception Si algún dato es inválido, el contrato no existe o
     *                   falla la eliminación.
     */
    public void eliminarContratoVigilancia(String codigoSucursal, String codigoVigilante, String fechaStr)
            throws Exception {
        if (codigoSucursal == null || codigoSucursal.trim().isEmpty()
                || codigoVigilante == null || codigoVigilante.trim().isEmpty()
                || fechaStr == null || fechaStr.trim().isEmpty()) {
            throw new Exception("Sucursal, vigilante y fecha son obligatorios para eliminar un contrato.");
        }

        try {
            LocalDate fecha = LocalDate.parse(fechaStr);
            String idContrato = codigoSucursal.trim() + "-" + codigoVigilante.trim() + "-" + fecha;

            contratoDAO.buscarPorId(idContrato);
            contratoDAO.eliminar(idContrato);
        } catch (ObjetoNoEncontradoException e) {
            throw new Exception("No existe un contrato con los datos ingresados.");
        } catch (java.time.format.DateTimeParseException e) {
            throw new Exception("Formato de fecha inválido. Por favor use el formato YYYY-MM-DD.");
        } catch (ErrorAlEliminarException e) {
            throw new Exception(e.getMessage());
        } catch (ErrorAlLeerException e) {
            throw new Exception("No se pudo leer la información de contratos: " + e.getMessage());
        }
    }

    /**
     * Elimina una entidad bancaria siempre que no tenga sucursales asociadas.
     *
     * @param codigo Código de la entidad bancaria a eliminar.
     * @throws Exception Si el código es inválido, la entidad no existe, tiene
     *                   sucursales asociadas o falla la eliminación.
     */
    public void eliminarEntidadBancaria(String codigo) throws Exception {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new Exception("El código de la entidad bancaria es obligatorio.");
        }

        try {
            entidadBancariaDAO.buscarPorId(codigo);

            List<Sucursal> sucursales = sucursalDAO.obtenerTodos();
            boolean tieneSucursales = sucursales.stream()
                    .anyMatch(sucursal -> sucursal.getEntidad().getCodigo().equals(codigo));

            if (tieneSucursales) {
                throw new Exception("No se puede eliminar la entidad bancaria porque tiene sucursales asociadas.");
            }

            entidadBancariaDAO.eliminar(codigo);
        } catch (ObjetoNoEncontradoException e) {
            throw new Exception("No existe una entidad bancaria con el código: " + codigo);
        } catch (ErrorAlLeerException e) {
            throw new Exception("No se pudieron verificar las sucursales: " + e.getMessage());
        } catch (ErrorAlEliminarException e) {
            throw new Exception(e.getMessage());
        }
    }
}
