package daos;

import exceptions.ErrorAlActualizarException;
import exceptions.ErrorAlEliminarException;
import exceptions.ErrorAlGuardarException;
import exceptions.ErrorAlLeerException;
import exceptions.ObjetoNoEncontradoException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.ContratoVigilancia;

public class ContratoVigilanciaDAO implements IGenericDAO<ContratoVigilancia> {

    private final String RUTA_ARCHIVO = "contratos_vigilancia.txt";

    public ContratoVigilanciaDAO() {
        crearArchivoSiNoExiste();
    }

    private void crearArchivoSiNoExiste() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error al crear el archivo de contratos: " + e.getMessage());
        }
    }

    // Armo un ID único uniendo los 3 datos principales, capaz sirve para busquedas 
    private String generarIdCompuesto(ContratoVigilancia c) {
        return c.getCodigoSucursal() + "-" + c.getCodigoVigilante() + "-" + c.getFecha().toString();
    }

    @Override
    public void guardar(ContratoVigilancia entidad) throws ErrorAlGuardarException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            String linea = entidad.getCodigoSucursal() + "," + 
                           entidad.getCodigoVigilante() + "," + 
                           entidad.getFecha().toString() + "," + 
                           entidad.isConArma();
            
            bw.write(linea);
            bw.newLine(); 
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Contrato Vigilancia", e.getMessage());
        }
    }

    @Override
    public List<ContratoVigilancia> obtenerTodos() throws ErrorAlLeerException {
        List<ContratoVigilancia> listaContratos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
            String linea;
            
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");

                if (partes.length == 4) {
                    String codigoSucursal = partes[0];
                    String codigoVigilante = partes[1];
                    //Parseo fecha y  booleano del txt 
                    LocalDate fecha = LocalDate.parse(partes[2]); 
                    boolean conArma = Boolean.parseBoolean(partes[3]);

                    ContratoVigilancia contrato = new ContratoVigilancia(codigoSucursal, codigoVigilante, fecha, conArma);
                    listaContratos.add(contrato);
                }
            }
        } catch (IOException e) {
            throw new ErrorAlLeerException("Archivo de Contratos", e.getMessage());
        }
        return listaContratos;
    }

    @Override
    public ContratoVigilancia buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException {
        List<ContratoVigilancia> contratos = obtenerTodos();
        for (ContratoVigilancia contrato : contratos) {
            // Comparo el ID compuesto que llegue con el generado 
            if (generarIdCompuesto(contrato).equals(id)) {
                return contrato; 
            }
        }
        throw new ObjetoNoEncontradoException("Contrato Vigilancia", id);
    }

    @Override
    public void actualizar(ContratoVigilancia entidad) throws ErrorAlActualizarException {
        List<ContratoVigilancia> contratos;

        try {
            contratos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException("Contrato Vigilancia", "No se pudo leer el archivo original: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (ContratoVigilancia c : contratos) {
                if (generarIdCompuesto(c).equals(generarIdCompuesto(entidad))) {
                    bw.write(entidad.getCodigoSucursal() + "," + 
                             entidad.getCodigoVigilante() + "," + 
                             entidad.getFecha().toString() + "," + 
                             entidad.isConArma());
                } else {
                    bw.write(c.getCodigoSucursal() + "," + 
                             c.getCodigoVigilante() + "," + 
                             c.getFecha().toString() + "," + 
                             c.isConArma());
                }
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ErrorAlActualizarException("Contrato Vigilancia", "No se pudo escribir en el archivo: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(String id) throws ErrorAlEliminarException {
        List<ContratoVigilancia> contratos;

        try {
            contratos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException("Contrato Vigilancia", "No se pudo leer el archivo original: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (ContratoVigilancia c : contratos) {
                if (!generarIdCompuesto(c).equals(id)) { 
                    bw.write(c.getCodigoSucursal() + "," + 
                             c.getCodigoVigilante() + "," + 
                             c.getFecha().toString() + "," + 
                             c.isConArma());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new ErrorAlEliminarException("Contrato Vigilancia", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }
}