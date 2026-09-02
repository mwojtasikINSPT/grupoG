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
import daos.IContratoVigilanciaDAO;
import daos.IGenericDAO;

/**
 * Controlador encargado de gestionar las operaciones bancarias, incluyendo el
 * registro y listado de vigilantes, sucursales y contratos de vigilancia.
 */
public class BancarioController
     {
   private final IGenericDAO<Sucursal> sucursalDAO;
    private final IGenericDAO<Vigilante> vigilanteDAO;
    private final IContratoVigilanciaDAO contratoDAO;
   private final IGenericDAO<EntidadBancaria> entidadBancariaDAO;

        /**
 * Crea el controlador con los DAO utilizados por la aplicación.
 */
public BancarioController() {
    this(
            new SucursalDAO(),
            new VigilanteDAO(),
            new ContratoVigilanciaDAO(),
            new EntidadBancariaDAO()
    );
}

/**
 * Crea el controlador con implementaciones de acceso a datos externas.
 * Esto permite cambiar el almacenamiento sin modificar el controlador.
 *
 * @param sucursalDAO acceso a los datos de sucursales
 * @param vigilanteDAO acceso a los datos de vigilantes
 * @param contratoDAO acceso a los contratos de vigilancia
 * @param entidadBancariaDAO acceso a las entidades bancarias
 */
public BancarioController(
       IGenericDAO<Sucursal> sucursalDAO,
        IGenericDAO<Vigilante> vigilanteDAO,
        IContratoVigilanciaDAO contratoDAO,
       IGenericDAO<EntidadBancaria> entidadBancariaDAO) {

    this.sucursalDAO = sucursalDAO;
    this.vigilanteDAO = vigilanteDAO;
    this.contratoDAO = contratoDAO;
    this.entidadBancariaDAO = entidadBancariaDAO;
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
        if (codigo == null) {
            throw new Exception("El código del vigilante no puede estar vacío.");
        }

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
        if (codigo == null || domicilioCentral == null) {
            throw new Exception("El código y el domicilio central son obligatorios.");
        }

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
   /**
 * Registra una sucursal vinculada a una entidad bancaria existente.
 *
 * @param codigoSucursal código identificador de la sucursal
 * @param domicilio domicilio físico de la sucursal
 * @param numEmpleado cantidad de empleados
 * @param codigoBanco código de la entidad bancaria asociada
 * @throws Exception si los datos son inválidos, la entidad no existe,
 *                   la sucursal está repetida o no puede guardarse
 */

public void registrarSucursal(
        String codigoSucursal,
        String domicilio,
        int numEmpleado,
        String codigoBanco) throws Exception {

    if (codigoSucursal == null
            || codigoSucursal.trim().isEmpty()
            || domicilio == null
            || domicilio.trim().isEmpty()
            || codigoBanco == null
            || codigoBanco.trim().isEmpty()) {

        throw new Exception(
                "Código, domicilio y entidad bancaria son obligatorios."
        );
    }

    if (numEmpleado < 0) {
        throw new Exception(
                "La cantidad de empleados no puede ser negativa."
        );
    }

    String idSucursal = codigoSucursal.trim();
    String idEntidad = codigoBanco.trim();

    try {
        try {
            sucursalDAO.buscarPorId(idSucursal);

            throw new Exception(
                    "La sucursal '" + idSucursal
                    + "' ya está registrada."
            );
        } catch (ObjetoNoEncontradoException e) {
            // No encontrarla indica que el código está disponible.
        }

        // Comprueba que la entidad bancaria relacionada exista.
        entidadBancariaDAO.buscarPorId(idEntidad);

        Sucursal nuevaSucursal = new Sucursal(
                idSucursal,
                domicilio.trim(),
                numEmpleado,
                idEntidad
        );

        sucursalDAO.guardar(nuevaSucursal);

    } catch (ObjetoNoEncontradoException e) {
        throw new Exception(
                "La entidad bancaria '" + idEntidad
                + "' no existe."
        );
    } catch (ErrorAlLeerException | ErrorAlGuardarException e) {
        throw new Exception(e.getMessage());
    }
} 
/**
 * Registra un contrato de vigilancia.
 *
 * @param codigoSucursal código de la sucursal
 * @param codigoVigilante código del vigilante
 * @param fechaStr fecha del contrato con formato AAAA-MM-DD
 * @param conArma indica si el vigilante porta un arma
 * @throws Exception si los datos son inválidos, las entidades no existen
 *                   o el contrato ya está registrado
 */


public void registrarContratoVigilancia(
        String codigoSucursal,
        String codigoVigilante,
        String fechaStr,
        boolean conArma) throws Exception {

    if (codigoSucursal == null
            || codigoVigilante == null
            || fechaStr == null
            || codigoSucursal.trim().isEmpty()
            || codigoVigilante.trim().isEmpty()
            || fechaStr.trim().isEmpty()) {

        throw new Exception(
                "Sucursal, vigilante y fecha son obligatorios."
        );
    }

    String idSucursal = codigoSucursal.trim();
    String idVigilante = codigoVigilante.trim();

    try {
        LocalDate fecha = LocalDate.parse(fechaStr.trim());

        // Comprueba que las entidades relacionadas existan.
        sucursalDAO.buscarPorId(idSucursal);
        vigilanteDAO.buscarPorId(idVigilante);

        String idContrato = idSucursal
                + "-"
                + idVigilante
                + "-"
                + fecha;

        if (contratoDAO.existe(idContrato)) {
            throw new Exception(
                    "El contrato '" + idContrato
                    + "' ya está registrado."
            );
        }

        ContratoVigilancia nuevoContrato =
                new ContratoVigilancia(
                        idSucursal,
                        idVigilante,
                        fecha,
                        conArma
                );

        contratoDAO.guardar(nuevoContrato);

    } catch (ObjetoNoEncontradoException e) {
        throw new Exception(
                "Entidad no encontrada: " + e.getMessage()
        );
    } catch (java.time.format.DateTimeParseException e) {
        throw new Exception(
                "Formato de fecha inválido. Use AAAA-MM-DD."
        );
    } catch (ErrorAlLeerException | ErrorAlGuardarException e) {
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
     * 
     * El proceso realiza las siguientes validaciones antes de la eliminación:
     * <ul>
     * <li>Verifica que el código proporcionado no sea nulo ni esté vacío.</li>
     * <li>Comprueba la existencia del vigilante en la base de datos.</li>
     * <li>Valida que el vigilante no esté vinculado a ningún contrato activo o
     * registrado.</li>
     * </ul> 
     * 
     * @param codigo El código único que identifica al vigilante que se desea
     *               eliminar. No debe ser {null} ni una cadena vacía.
     * @throws Exception Si el código es inválido, si el vigilante no existe, si el
     *         vigilante tiene contratos asociados, o si ocurre un error interno en
     *         la base de datos.
     */
  
    public void eliminarVigilante(String codigo) throws Exception {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new Exception("El código del vigilante es obligatorio.");
        }

        try {
            vigilanteDAO.buscarPorId(codigo);

            List<ContratoVigilancia> contratos = contratoDAO.obtenerTodos();

            boolean tieneContratos = contratos.stream()
        .anyMatch(contrato ->
                codigo.equals(contrato.getIdVigilante()));

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
        .anyMatch(contrato ->
                codigo.equals(contrato.getIdSucursal()));

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
                  .anyMatch(sucursal ->
        codigo.equals(sucursal.getIdEntidadBancaria()));

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
