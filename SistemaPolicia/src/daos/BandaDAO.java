package daos;

import exceptions.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import models.Banda;

/**
 * Data Access Object para la gestión de entidades {@link Banda} en persistencia
 * de archivos. Implementa las operaciones CRUD básicas sobre el archivo
 * bandas.txt.
 */
public class BandaDAO implements IGenericDAO<Banda> {

    // Ruta del archivo 
    private final String RUTA_ARCHIVO = "bandas.txt";

    /**
     * Constructor: Se ejecuta apenas creamos el DAO.
     */
    public BandaDAO() {
        crearArchivoSiNoExiste();
    }

    /**
     * Método para la creación del archivo.
     */
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

    /**
     * Método auxiliar que centraliza el formato de texto para guardar en el
     * archivo CSV.
     *
     * @param b La banda a formatear.
     * @return String con los datos separados por comas.
     */
    private String formatearParaArchivo(Banda b) {
        return b.getNumeroBanda() + "," + b.getCantMiembros();
    }

    /**
     * Verifica si una banda existe en el sistema.
     *
     * @param id El número de banda a verificar.
     * @return true si la banda existe, false en caso contrario.
     */
    public boolean existe(String id) {
        try {
            buscarPorId(id);
            return true;
        } catch (ObjetoNoEncontradoException | ErrorAlLeerException e) {
            return false;
        }
    }

    // ==========================================
    // IMPLEMENTACIÓN DE LOS MÉTODOS DE LA INTERFAZ
    // ==========================================
    /**
     * Guarda una nueva banda al final del archivo.
     *
     * @param entidad La {@link Banda} a persistir.
     * @throws ErrorAlGuardarException si el ID ya existe o hay un error de
     * escritura.
     */
    @Override
    public void guardar(Banda entidad) throws ErrorAlGuardarException {
        if (existe(entidad.getNumeroBanda())) {
            throw new ErrorAlGuardarException("Banda", "Ya existe una banda con el número " + entidad.getNumeroBanda());
        }
        // El 'true' en FileWriter significa modo "Append" (agrega al final sin borrar lo anterior)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            bw.write(formatearParaArchivo(entidad));
            bw.newLine(); // Salto de línea para el próximo registro
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Banda", e.getMessage());
        }
    }

    /**
     * Recupera todas las bandas del archivo.
     * @return Lista de objetos {@link Banda}.
     * @throws ErrorAlLeerException si ocurre un error durante la lectura.
     */
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

    /**
     * Busca una banda por su ID (número de banda).
     * @param id El número de la banda.
     * @return La {@link Banda} encontrada.
     * @throws ObjetoNoEncontradoException si no existe.
     * @throws ErrorAlLeerException si ocurre un error de lectura.
     */
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

    /**
     * Actualiza los datos de una banda existente.
     * @param entidad La banda con los datos actualizados.
     * @throws ErrorAlActualizarException si ocurre un error de E/S.
     */
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
                    bw.write(formatearParaArchivo(entidad));
                } else {
                    bw.write(formatearParaArchivo(b));
                }
                bw.newLine();
            }
        }catch (IOException e) {
            throw new ErrorAlActualizarException("Banda", "No se pudo escribir en el archivo: " + e.getMessage());
        }
    }

    /**
     * Elimina una banda del registro mediante su ID.
     * @param id El número de la banda a eliminar.
     * @throws ErrorAlEliminarException si ocurre un error de E/S.
     */
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
                    bw.write(formatearParaArchivo(b));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new ErrorAlEliminarException("Banda", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }

}
