package controllers;

import daos.ContratoVigilanciaDAO;
import daos.SucursalDAO;
import daos.VigilanteDAO;
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

    /**
     * Constructor que inicializa las instancias de los DAOs necesarios para la
     * gestión bancaria.
     */
    public BancarioController() {
        this.sucursalDAO = new SucursalDAO();
        this.vigilanteDAO = new VigilanteDAO();
        this.contratoDAO = new ContratoVigilanciaDAO();
    }

    /**
     * Registra un nuevo vigilante en el sistema realizando validaciones de
     * datos.
     *
     * @param codigo Código identificador del vigilante.
     * @param edad Edad del vigilante.
     * @throws Exception Si el código está vacío, el vigilante es menor de edad
     * o ya existe.
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
                //Si no lo encuentra, se puede crear
            }

            Vigilante nuevoVigilante = new Vigilante(codigo, edad);
            vigilanteDAO.guardar(nuevoVigilante);
        } catch (ErrorAlGuardarException e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Registra una nueva sucursal vinculada a una entidad bancaria.
     *
     * @param codigoSucursal Código identificador de la sucursal.
     * @param domicilio Domicilio físico de la sucursal.
     * @param numEmpleado Cantidad de empleados.
     * @param codigoBanco Código de la entidad bancaria asociada.
     * @throws Exception Si los códigos están vacíos o la sucursal ya existe.
     */
    public void registrarSucursal(String codigoSucursal, String domicilio, int numEmpleado, String codigoBanco) throws Exception {
        if (codigoSucursal.trim().isEmpty() || codigoBanco.trim().isEmpty()) {
            throw new Exception("El código de sucursal y el código de banco son obligatorios.");
        }
        try {
            try {
                sucursalDAO.buscarPorId(codigoSucursal);
                throw new Exception("La sucursal '" + codigoSucursal + "' ya existe.");
            } catch (ObjetoNoEncontradoException e) {
            }
            EntidadBancaria bancoAsociado = new EntidadBancaria(codigoBanco, "");
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
     * @param codigoSucursal Código de la sucursal.
     * @param codigoVigilancia Código identificador único del contrato.
     * @param codigoVigilante Código del vigilante a contratar.
     * @param fechaStr Fecha de inicio en formato YYYY-MM-DD.
     * @param conArma Indica si el vigilante porta arma.
     * @throws Exception Si la sucursal o vigilante no existen, el contrato está
     * duplicado o la fecha es inválida.
     */
    public void registrarContratoVigilancia(String codigoSucursal, String codigoVigilancia, String codigoVigilante, String fechaStr, boolean conArma) throws Exception {
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

            //Fecha
            LocalDate fecha = LocalDate.parse(fechaStr);

            //Armar contrato
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
}
