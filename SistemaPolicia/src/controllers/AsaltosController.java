package controllers;

//Aca relacionamos AsaltoDAO, AsaltanteDAO, BandaDAO y SucursalDAO.

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

//cargar bandas, registrar asaltantes o asaltos
public class AsaltosController {
    private final BandaDAO bandaDAO;
    private final AsaltanteDAO asaltanteDAO;
    private final AsaltoDAO asaltoDAO;
    private final SucursalDAO sucursalDAO;

    public AsaltosController() {
        this.bandaDAO = new BandaDAO();
        this.asaltanteDAO = new AsaltanteDAO();
        this.asaltoDAO = new AsaltoDAO();
        this.sucursalDAO = new SucursalDAO();
    }
    
    //Registrar una nueva banda
    public void registrarBanda(String numeroBanda, int cantMiembro) throws Exception{
            if(numeroBanda.trim().isEmpty()){
            throw new Exception("El numero de banda no puede estar vacío.");
        }
        
        if(cantMiembro < 0){
            throw new Exception("La cantidad de miembros no puede ser negativa.");
        }
        
        try{
            try{
                bandaDAO.buscarPorId(numeroBanda);
                throw new Exception("La banda número '" + numeroBanda + "' ya está registrada.");
            }catch(ObjetoNoEncontradoException e){
                //Si no la encuentra, se puede crear
            }

            Banda nuevaBanda= new Banda(numeroBanda, cantMiembro);
            bandaDAO.guardar(nuevaBanda);
        } catch (ErrorAlGuardarException e){
            throw new Exception(e.getMessage());
        }
    }
    
    //Registrar un asaltante y asignarle una banda existente
    public void registrarAsaltante(String clave, String nombreCompleto, String numeroBanda) throws Exception{
           try{
           // Verificar que la banda existe
           Banda banda;
           try{
               banda = bandaDAO.buscarPorId(numeroBanda);
           }catch(ObjetoNoEncontradoException e){
               throw new Exception("La banda '" + numeroBanda + "' no existe.");
           }
           // Verificar que la clave del asaltante no este duplicado
           
           try{
               asaltanteDAO.buscarPorId(clave);
               throw new Exception("La clave del asaltante '" + clave + "' ya se encuentra en uso.");
           }catch(ObjetoNoEncontradoException e){
           }
           
           //Armar el asaltante asociando la banda
           Asaltante nuevoAsaltante = new Asaltante(clave, nombreCompleto, banda);
           asaltanteDAO.guardar(nuevoAsaltante);
       }catch (ErrorAlGuardarException e){
           throw new Exception(e.getMessage());
       }
   }
    
   
    //Registrar un asalto vinculando asaltante, sucursal, y fecha.
    public void registrarAsalto(String idAsalto, String claveAsaltante, String codigoSucursal, String fechaStr) throws Exception{
           try{
           // Verificar que el asaltante existe
           Asaltante asaltante;
           try{
               asaltante = asaltanteDAO.buscarPorId(claveAsaltante);
           }catch(ObjetoNoEncontradoException e){
               throw new Exception("El asaltante '" + claveAsaltante + "' no existe.");
           }
           // Verificar que la sucursal existe
           Sucursal sucursal;
           try{
               sucursal = sucursalDAO.buscarPorId(codigoSucursal);
           }catch(ObjetoNoEncontradoException e){
               throw new Exception("La sucursal '" + codigoSucursal + "' no existe.");
           }
           // Verigicar que la id del asalto no sea duplicado.
           try{
               asaltoDAO.buscarPorId(idAsalto);
           }catch(ObjetoNoEncontradoException e){
               throw new Exception("El id de asalto '" + idAsalto + "' ya está en uso.");
           }
           
           //Fecha
           LocalDate fecha = LocalDate.parse(fechaStr);
           
           //Armar asalto
           Asalto nuevoAsalto = new Asalto(idAsalto, asaltante, sucursal, fecha);
           asaltoDAO.guardar(nuevoAsalto);
       }catch (java.time.format.DateTimeParseException e){
           throw new Exception("Formato de fecha inválido. Por favor use el formato  YYYY-MM-DD.");
       }catch (ErrorAlGuardarException e){
           throw new Exception(e.getMessage());
       }
    }
    
    //Lista de todos los asaltos
    public List <Asalto> listarAsaltos() throws Exception{ 
       try{
           return asaltoDAO.obtenerTodos();
       }catch(ErrorAlLeerException e){
           throw new Exception(e.getMessage());
       }
   } 
}
