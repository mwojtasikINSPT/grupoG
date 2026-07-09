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
import models.Vigilante;

public class VigilanteDAO implements IGenericDAO<Vigilante> {

    // Ruta del archivo 
    private final String RUTA_ARCHIVO = "vigilantes.txt";

    public VigilanteDAO() {
        crearArchivoSiNoExiste();
    }

    private void crearArchivoSiNoExiste() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error al crear el archivo de vigilantes: " + e.getMessage());
        }
    }

    @Override
    public void guardar(Vigilante entidad) throws ErrorAlGuardarException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            
            // Armo la línea separada por comas
            String linea = entidad.getCodigo() + "," + 
                           entidad.getEdad();
            
            bw.write(linea);
            bw.newLine(); 
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Vigilante", e.getMessage());
        }
    }

    @Override
    public List<Vigilante> obtenerTodos() throws ErrorAlLeerException {
        List<Vigilante> listaVigilantes = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
            String linea;
            
            // Leo línea por línea hasta que el archivo termine
            while ((linea = br.readLine()) != null) {
                
                // Separo los datos usando la coma
                String[] partes = linea.split(",");

                // Verifico que la línea tenga exactamente los 2 datos
                if (partes.length == 2) {
                    String codigo = partes[0];
                    
                    // Parseo el texto a int para la edad
                    int edad = Integer.parseInt(partes[1]);

                    // Reconstruyo el objeto y lo agrego a la lista
                    Vigilante vigilante = new Vigilante(codigo, edad);
                    listaVigilantes.add(vigilante);
                }
            }
        } catch (IOException e) {
            throw new ErrorAlLeerException("Archivo de Vigilantes", e.getMessage());
        }
        return listaVigilantes;
    }

    @Override
    public Vigilante buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException {
        List<Vigilante> vigilantes = obtenerTodos();
        for (Vigilante vigilante : vigilantes) {
            // Busco usando el código como ID único
            if (vigilante.getCodigo().equals(id)) {
                return vigilante; 
            }
        }
        throw new ObjetoNoEncontradoException("Vigilante", id);
    }

    @Override
    public void actualizar(Vigilante entidad) throws ErrorAlActualizarException {
        List<Vigilante> vigilantes;

        // 1. Intento leer
        try {
            vigilantes = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException("Vigilante", "No se pudo leer el archivo original: " + e.getMessage());
        }

        // 2. Intento escribir (sobrescribir)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Vigilante v : vigilantes) {
                if (v.getCodigo().equals(entidad.getCodigo())) {
                    bw.write(entidad.getCodigo() + "," + 
                             entidad.getEdad());
                } else {
                    bw.write(v.getCodigo() + "," + 
                             v.getEdad());
                }
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ErrorAlActualizarException("Vigilante", "No se pudo escribir en el archivo: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(String id) throws ErrorAlEliminarException {
        List<Vigilante> vigilantes;

        // 1. Intento leer
        try {
            vigilantes = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException("Vigilante", "No se pudo leer el archivo original: " + e.getMessage());
        }

        // 2. Intento escribir (sobrescribir omitiendo el id)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Vigilante v : vigilantes) {
                if (!v.getCodigo().equals(id)) { 
                    bw.write(v.getCodigo() + "," + 
                             v.getEdad());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new ErrorAlEliminarException("Vigilante", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }
}