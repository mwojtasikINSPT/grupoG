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
import models.Asalto;

public class AsaltoDAO implements IGenericDAO<Asalto> {

    // Ruta del archivo 
    private final String RUTA_ARCHIVO = "asaltos.txt";

    public AsaltoDAO() {
        crearArchivoSiNoExiste();
    }

    private void crearArchivoSiNoExiste() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error al crear el archivo de asaltos: " + e.getMessage());
        }
    }

    @Override
    public void guardar(Asalto entidad) throws ErrorAlGuardarException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            // Transformo el LocalDate a String automáticamente usando  método toString()
            String linea = entidad.getIdAsalto() + "," + 
                           entidad.getClaveAsaltante() + "," + 
                           entidad.getCodigoSucursal() + "," + 
                           entidad.getFecha().toString();
            
            bw.write(linea);
            bw.newLine(); 
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Asalto", e.getMessage());
        }
    }

    @Override
    public List<Asalto> obtenerTodos() throws ErrorAlLeerException {
        List<Asalto> listaAsaltos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
            String linea;
            
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");

                // El modelo tiene 4 atributos, verifico que haya 4 partes
                if (partes.length == 4) {
                    String idAsalto = partes[0];
                    String claveAsaltante = partes[1];
                    String codigoSucursal = partes[2];
                    // Yo convierto el texto nuevamente a LocalDate
                    LocalDate fecha = LocalDate.parse(partes[3]); 

                    Asalto asalto = new Asalto(idAsalto, claveAsaltante, codigoSucursal, fecha);
                    listaAsaltos.add(asalto);
                }
            }
        } catch (IOException e) {
            throw new ErrorAlLeerException("Archivo de Asaltos", e.getMessage());
        }
        return listaAsaltos;
    }

    @Override
    public Asalto buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException {
        List<Asalto> asaltos = obtenerTodos();
        for (Asalto asalto : asaltos) {
            if (asalto.getIdAsalto().equals(id)) {
                return asalto; 
            }
        }
        throw new ObjetoNoEncontradoException("Asalto", id);
    }

    @Override
    public void actualizar(Asalto entidad) throws ErrorAlActualizarException {
        List<Asalto> asaltos;

        try {
            asaltos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException("Asalto", "No se pudo leer el archivo original: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Asalto a : asaltos) {
                if (a.getIdAsalto().equals(entidad.getIdAsalto())) {
                    bw.write(entidad.getIdAsalto() + "," + 
                             entidad.getClaveAsaltante() + "," + 
                             entidad.getCodigoSucursal() + "," + 
                             entidad.getFecha().toString());
                } else {
                    bw.write(a.getIdAsalto() + "," + 
                             a.getClaveAsaltante() + "," + 
                             a.getCodigoSucursal() + "," + 
                             a.getFecha().toString());
                }
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ErrorAlActualizarException("Asalto", "No se pudo escribir en el archivo: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(String id) throws ErrorAlEliminarException {
        List<Asalto> asaltos;

        try {
            asaltos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException("Asalto", "No se pudo leer el archivo original: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Asalto a : asaltos) {
                if (!a.getIdAsalto().equals(id)) { 
                    bw.write(a.getIdAsalto() + "," + 
                             a.getClaveAsaltante() + "," + 
                             a.getCodigoSucursal() + "," + 
                             a.getFecha().toString());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new ErrorAlEliminarException("Asalto", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }
}