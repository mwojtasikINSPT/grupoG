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
import java.util.ArrayList;
import java.util.List;
import models.Juez;

public class JuezDAO implements IGenericDAO<Juez> {

    // Ruta del archivo 
    private final String RUTA_ARCHIVO = "jueces.txt";

    public JuezDAO() {
        crearArchivoSiNoExiste();
    }

    private void crearArchivoSiNoExiste() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error al crear el archivo de jueces: " + e.getMessage());
        }
    }

    @Override
    public void guardar(Juez entidad) throws ErrorAlGuardarException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            
            // Armo la línea separada por comas
            String linea = entidad.getClaveInterna() + "," + 
                           entidad.getAniosServicio() + "," + 
                           entidad.getNombre();
            
            bw.write(linea);
            bw.newLine(); 
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Juez", e.getMessage());
        }
    }

    @Override
    public List<Juez> obtenerTodos() throws ErrorAlLeerException {
        List<Juez> listaJueces = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
            String linea;
            
            // Leo línea por línea hasta que el archivo termine
            while ((linea = br.readLine()) != null) {
                
                // Separo los datos usando la coma
                String[] partes = linea.split(",");

                // Verifico que la línea tenga exactamente los 3 datos
                if (partes.length == 3) {
                    String claveInterna = partes[0];
                    
                    // Parseo el texto a int para los años de servicio
                    int aniosServicio = Integer.parseInt(partes[1]);
                    
                    String nombre = partes[2];

                    // Reconstruyo el objeto y lo agrego a la lista
                    Juez juez = new Juez(claveInterna, aniosServicio, nombre);
                    listaJueces.add(juez);
                }
            }
        } catch (IOException e) {
            throw new ErrorAlLeerException("Archivo de Jueces", e.getMessage());
        }
        return listaJueces;
    }

    @Override
    public Juez buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException {
        List<Juez> jueces = obtenerTodos();
        for (Juez juez : jueces) {
            // Busco usando la clave interna como ID único
            if (juez.getClaveInterna().equals(id)) {
                return juez; 
            }
        }
        throw new ObjetoNoEncontradoException("Juez", id);
    }

    @Override
    public void actualizar(Juez entidad) throws ErrorAlActualizarException {
        List<Juez> jueces;

        // 1. Intento leer
        try {
            jueces = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException("Juez", "No se pudo leer el archivo original: " + e.getMessage());
        }

        // 2. Intento escribir (sobrescribir)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Juez j : jueces) {
                if (j.getClaveInterna().equals(entidad.getClaveInterna())) {
                    bw.write(entidad.getClaveInterna() + "," + 
                             entidad.getAniosServicio() + "," + 
                             entidad.getNombre());
                } else {
                    bw.write(j.getClaveInterna() + "," + 
                             j.getAniosServicio() + "," + 
                             j.getNombre());
                }
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ErrorAlActualizarException("Juez", "No se pudo escribir en el archivo: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(String id) throws ErrorAlEliminarException {
        List<Juez> jueces;

        // 1. Intento leer
        try {
            jueces = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException("Juez", "No se pudo leer el archivo original: " + e.getMessage());
        }

        // 2. Intento escribir (sobrescribir omitiendo el id)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Juez j : jueces) {
                if (!j.getClaveInterna().equals(id)) { 
                    bw.write(j.getClaveInterna() + "," + 
                             j.getAniosServicio() + "," + 
                             j.getNombre());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new ErrorAlEliminarException("Juez", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }
}