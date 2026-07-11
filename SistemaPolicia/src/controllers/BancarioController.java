package controllers;

//Manejaremos DAOs reativos a Bancos: SucursalDAO, EntidadBancariaDAO, VigilanteDAO, ContratoVigilanciaDAO

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

//cargar sucursales, vigilantes, contratos
public class BancarioController {

    private final SucursalDAO sucursalDAO;
    private final VigilanteDAO vigilanteDAO;
    private final ContratoVigilanciaDAO contratoDAO;
    
    //Constructor para inicializar los DAOs
    public BancarioController() {
        this.sucursalDAO = new SucursalDAO();
        this.vigilanteDAO = new VigilanteDAO();
        this.contratoDAO = new ContratoVigilanciaDAO();
    }
    
    // Registro nuevo vigilante validando datos    
    public void registrarVigilante(String codigo, int edad) throws Exception{
        if(codigo.trim().isEmpty()){
            throw new Exception("El código del vigilante no puede estar vacío.");
        }
        
        if(edad < 18){
            throw new Exception("El vigilante debe ser mayor de edad (mínimo 18 años.)");
        }
        
        try{
            try{
                vigilanteDAO.buscarPorId(codigo);
                throw new Exception("El código de vigilante '" + codigo + "' ya está registrado.");
            }catch(ObjetoNoEncontradoException e){
                //Si no lo encuentra, se puede crear
            }

            Vigilante nuevoVigilante = new Vigilante(codigo, edad);
            vigilanteDAO.guardar(nuevoVigilante);
        } catch (ErrorAlGuardarException e){
            throw new Exception(e.getMessage());
        }
    }
        
    
    // Registrar  nueva sucursal vinculada a un Banco validando datos
   public void registrarSucursal(String codigoSucursal, String domicilio, int numEmpleado, String codigoBanco) throws Exception{
       if(codigoSucursal.trim().isEmpty() || codigoBanco.trim().isEmpty()){
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
        } catch (ErrorAlGuardarException e){
            throw new Exception(e.getMessage());
        }
   }
   
   
   // Registrar un contrato de vigilancia (vigilante, sucursal y fecha)
   public void registrarContratoVigilancia(String codigoSucursal, String codigoVigilancia, String codigoVigilante, String fechaStr, boolean conArma) throws Exception{
       try{
           // Verificar que la sucursal existe
           Sucursal sucursal;
           try{
               sucursal = sucursalDAO.buscarPorId(codigoSucursal);
           }catch(ObjetoNoEncontradoException e){
               throw new Exception("La sucursal '" + codigoSucursal + "' no existe.");
           }
           // Verigicar que el vigilante exista
           Vigilante vigilante;
           try{
               vigilante = vigilanteDAO.buscarPorId(codigoVigilante);
           }catch(ObjetoNoEncontradoException e){
               throw new Exception("El vigilante '" + codigoVigilante + "' no existe");
           }
           
           //Fecha
           LocalDate fecha = LocalDate.parse(fechaStr);
           
           //Armar contrato
           ContratoVigilancia nuevoContrato = new ContratoVigilancia(sucursal, vigilante, fecha, conArma);
           contratoDAO.guardar(nuevoContrato);
       }catch (java.time.format.DateTimeParseException e){
           throw new Exception("Formato de fecha inválido. Por favor use el formato YYYY-MM-DD.");
       }catch (ErrorAlGuardarException e){
           throw new Exception(e.getMessage());
       }
   }
   
   //Lista de contratos
   public List <ContratoVigilancia> listarContratosVigilancia() throws Exception{ 
       try{
           return contratoDAO.obtenerTodos();
       }catch(ErrorAlLeerException e){
           throw new Exception(e.getMessage());
       }
   }
   
   // Lista de todos los vigilantes registrados operativamente
    public List<Vigilante> listarVigilantes() throws Exception {
        try {
            return vigilanteDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception("Error al recuperar la lista de vigilantes: " + e.getMessage());
        }
    }
    
    // Lista de todas las sucursales bancarias registradas
    public List<Sucursal> listarSucursales() throws Exception {
        try {
            return sucursalDAO.obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new Exception("Error al recuperar la lista de sucursales: " + e.getMessage());
        }
    }
   
    //Busco Vigilante x id
    public Vigilante buscarVigilantePorId(String codigo) throws Exception {
    return vigilanteDAO.buscarPorId(codigo);
}
}