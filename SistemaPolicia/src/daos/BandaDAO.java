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
import models.Banda;

public class BandaDAO implements IGenericDAO<Banda> {

    // Ruta del archivo 
    private final String RUTA_ARCHIVO = "bandas.txt";

    // Constructor: Se ejecuta apenas creamos el DAO
    public BandaDAO() {
        crearArchivoSiNoExiste();
    }

    // Método para la creación del archivo
    private void crearArchivoSiNoExiste() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (!archivo.exists()) {
                archivo.createNewFile(); // Creo el archivo en la carpeta del proyecto
            }
        } catch (IOException e) {
            System.out.println("Error al crear el archivo de bandas: " + e.getMessage());
        }
    }

    // ==========================================
    // IMPLEMENTACIÓN DE LOS MÉTODOS DE LA INTERFAZ
    // ==========================================
    @Override
    public void guardar(Banda entidad) throws ErrorAlGuardarException {
        // El 'true' en FileWriter significa modo "Append" (agrega al final sin borrar lo anterior)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            // Armamos la línea separada por comas
            String linea = entidad.getNumeroBanda() + "," + entidad.getCantMiembros();
            bw.write(linea);
            bw.newLine(); // Salto de línea para el próximo registro
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Banda", e.getMessage());
        }
    }

    @Override
    public List<Banda> obtenerTodos() throws ErrorAlLeerException {
        List<Banda> listaBandas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
            String linea;
            // Leo línea por línea hasta que el archivo termine 
            while ((linea = br.readLine()) != null) {
                // Separo los datos usando la coma 
                String[] partes = linea.split(",");

                // Verifico que la línea tenga exactamente los 2 datos
                if (partes.length == 2) {
                    String numeroBanda = partes[0];
                    int numeroMiembros = Integer.parseInt(partes[1]); // Transformo el String a int

                    // Reconstruyo el objeto y lo agrego a la lista
                    Banda banda = new Banda(numeroBanda, numeroMiembros);
                    listaBandas.add(banda);
                }
            }
        } catch (IOException e) {
            throw new ErrorAlLeerException("Archivo", e.getMessage());
        }
        return listaBandas;
    }

    @Override
    public Banda buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException {

        List<Banda> bandas = obtenerTodos();
        for (Banda banda : bandas) {
            if (banda.getNumeroBanda().equals(id)) {
                return banda; // Si encuentro la banda, la devuelvo
            }
        }
        throw new ObjetoNoEncontradoException("Banda", id);
    }

    @Override
    public void actualizar(Banda entidad) throws ErrorAlActualizarException {
        List<Banda> bandas;

        // 1. Intento leer
        try {
            bandas = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException("Banda", "No se pudo leer el archivo original: " + e.getMessage());
        }

        // 2. Intento escribir (sobrescribir)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Banda b : bandas) {
                if (b.getNumeroBanda().equals(entidad.getNumeroBanda())) {
                    bw.write(entidad.getNumeroBanda() + "," + entidad.getCantMiembros());
                } else {
                    bw.write(b.getNumeroBanda() + "," + b.getCantMiembros());
                }
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ErrorAlActualizarException("Banda", "No se pudo escribir en el archivo: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(String id) throws ErrorAlEliminarException {

        List<Banda> bandas;

        // 1. Intento leer
        try {
            bandas = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException("Banda", "No se pudo leer el archivo original: " + e.getMessage());
        }

        // 2. Intento escribir (sobrescribir omitiendo el id)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Banda b : bandas) {
                if (!b.getNumeroBanda().equals(id)) { 
                    bw.write(b.getNumeroBanda() + "," + b.getCantMiembros());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new ErrorAlEliminarException("Banda", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }

}
