package controllers;

//Manejaremos CasoJudicialDAO, JuezDAO. Cargar jueces y sentencias

import daos.CasoJudicialDAO;
import daos.JuezDAO;
import exceptions.ErrorAlGuardarException;
import exceptions.ObjetoNoEncontradoException;
import models.Asalto;
import models.CasoJudicial;
import models.Juez;

public class JudicialController {

    private final JuezDAO juezDAO;
    private final CasoJudicialDAO casoJudicialDAO;
    
    public JudicialController(){
        this.juezDAO = new JuezDAO();
        this.casoJudicialDAO = new CasoJudicialDAO();
    }
    
    //Registrar un nuevo Juez al sistema
    
    public void registrarJuez(String claveInterna, int aniosServicio, String nombre) throws Exception{
    
        if(claveInterna.trim().isEmpty() || nombre.trim().isEmpty()){
            throw new Exception("La clave interna y el nombre del juez no pueden estar vacíos.");
        }
        if(aniosServicio < 0){
            throw new Exception("Los años de servicio no pueden ser negativos.");
        }
        try {
            //Validar que no haya clave interna duplicada
            try{
                juezDAO.buscarPorId(claveInterna);
                throw new Exception("La clave de juez '" + claveInterna + "' ya está en uso.");
            }catch (ObjetoNoEncontradoException e){
                //Si no lo encuentra, se puede usar.
            }
            //Crear juez
            Juez nuevoJuez = new Juez(claveInterna, aniosServicio, nombre);
            juezDAO.guardar(nuevoJuez);       
        }catch(ErrorAlGuardarException e){
            throw new Exception("Error al guardar el registro del juez: " + e.getMessage());
        }
    }
    
    //Registrar veredicto final o expediente de asalto
    
    public void registrarCasoJudicial(String idAsalto, String claveJuez, boolean condenado, int mesesCarcel) throws Exception{
        if (idAsalto.trim().isEmpty() || claveJuez.trim().isEmpty()) {
            throw new Exception("El ID del asalto y la clave del juez son campos obligatorios.");
        }
        
        //Si no esta condenado, los meses en la carcel deben de ser 0
        int mesesEfectivos = condenado ? mesesCarcel : 0;
    
        if(condenado && mesesEfectivos <= 0){
            throw new Exception("Si el veredicto es CONDENADO, los meses en la cárcel deben ser mayores a 0.");
        }
        
        try {
            // Verificamos primero que el juez exista en el archivo de texto
            //Si existe, lo guardo en variable temporal
            Juez juezReal;
            try {
                juezReal = juezDAO.buscarPorId(claveJuez);
            } catch (ObjetoNoEncontradoException e) {
                throw new Exception("Operación inválida: El juez con clave '" + claveJuez + "' no existe en el sistema.");
            }
            
            Asalto asaltoTemp = new Asalto(idAsalto, null, null, null);
            CasoJudicial casoJucidial = new CasoJudicial(asaltoTemp, juezReal, condenado, mesesEfectivos);
            //crear el caso judicial
            CasoJudicial nuevoCaso = new CasoJudicial(asaltoTemp, juezReal, condenado, mesesEfectivos);
            // Guardo el caso en el archivo usando su DAO
            casoJudicialDAO.guardar(nuevoCaso);
            
        }catch (ErrorAlGuardarException e){
            throw new Exception("Error al guardar el caso judicial en el archivo: " + e.getMessage());
        }
    }
       
}
