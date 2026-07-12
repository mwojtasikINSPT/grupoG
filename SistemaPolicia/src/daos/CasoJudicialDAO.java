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
import models.Asalto;
import models.CasoJudicial;
import models.Juez;

/**
 * Data Access Object para la gestión de entidades {@link CasoJudicial} en
 * persistencia de archivos. Implementa las operaciones CRUD básicas sobre el
 * archivo casos_judiciales.txt.
 */
public class CasoJudicialDAO implements IGenericDAO<CasoJudicial> {

    // Ruta del archivo 
    private final String RUTA_ARCHIVO = "casos_judiciales.txt";

    /**
     * Constructor. Inicializa el DAO y asegura la existencia del archivo de
     * persistencia.
     */
    public CasoJudicialDAO() {
        crearArchivoSiNoExiste();
    }

    private void crearArchivoSiNoExiste() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error al crear el archivo de casos judiciales: " + e.getMessage());
        }
    }

    /**
     * Método auxiliar que centraliza el formato de texto para guardar en el
     * archivo CSV.
     *
     * @param c El caso judicial a formatear.
     * @return String con los datos separados por comas.
     */
    private String formatearParaArchivo(CasoJudicial c) {
        return c.getAsalto().getIdAsalto() + ","
                + c.getJuez().getClaveInterna() + ","
                + c.isCondenado() + ","
                + c.getMesesCarcel();
    }

    /**
     * Verifica si un caso judicial existe en el sistema basándose en el ID del
     * asalto.
     *
     * @param id El ID del asalto asociado al caso.
     * @return true si existe, false en caso contrario.
     */
    public boolean existe(String id) {
        try {
            buscarPorId(id);
            return true;
        } catch (ObjetoNoEncontradoException | ErrorAlLeerException e) {
            return false;
        }
    }

    /**
     * Guarda un nuevo caso judicial en el archivo, validando que no exista
     * previamente.
     *
     * @param entidad El objeto {@link CasoJudicial} a persistir.
     * @throws ErrorAlGuardarException si ocurre un error de escritura o
     * duplicidad.
     */
    @Override
    public void guardar(CasoJudicial entidad) throws ErrorAlGuardarException {
        if (existe(entidad.getAsalto().getIdAsalto())) {
            throw new ErrorAlGuardarException("Caso Judicial", "Ya existe un caso para el asalto ID " + entidad.getAsalto().getIdAsalto());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            bw.write(formatearParaArchivo(entidad));
            bw.newLine();
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Caso Judicial", e.getMessage());
        }
    }

    /**
     * Recupera todos los casos judiciales almacenados en el archivo.
     *
     * @return Lista de objetos {@link CasoJudicial}.
     * @throws ErrorAlLeerException si ocurre un error durante la lectura del
     * archivo.
     */
    @Override
    public List<CasoJudicial> obtenerTodos() throws ErrorAlLeerException {
        List<CasoJudicial> listaCasos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");

                if (partes.length == 4) {
                    String idAsalto = partes[0];
                    String claveJuez = partes[1];
                    // Convierto los textos a boolean e int respectivamente, "parseo"
                    boolean condenado = Boolean.parseBoolean(partes[2]);
                    int mesesCarcel = Integer.parseInt(partes[3]);

                    // Armo objetos temporales con los datos leídos
                    Asalto asalto = new Asalto(idAsalto, null, null, null);
                    Juez juez = new Juez(claveJuez, 0, "");

                    CasoJudicial caso = new CasoJudicial(asalto, juez, condenado, mesesCarcel);
                    listaCasos.add(caso);
                }
            }
        } catch (IOException e) {
            throw new ErrorAlLeerException("Archivo de Casos Judiciales", e.getMessage());
        }
        return listaCasos;
    }

    /**
     * Busca un caso judicial por el ID de su asalto asociado.
     *
     * @param id El ID del asalto.
     * @return El objeto {@link CasoJudicial} encontrado.
     * @throws ObjetoNoEncontradoException si no se encuentra el caso.
     * @throws ErrorAlLeerException si ocurre un error de lectura.
     */
    @Override
    public CasoJudicial buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException {
        List<CasoJudicial> casos = obtenerTodos();
        // Le pregunto el ID al objeto Asalto que está adentro del caso
        for (CasoJudicial caso : casos) {
            // Le pregunto el ID al objeto Asalto que está adentro del caso
            if (caso.getAsalto().getIdAsalto().equals(id)) {
                return caso;
            }
        }
        throw new ObjetoNoEncontradoException("Caso Judicial", id);
    }

    /**
     * Actualiza los datos de un caso judicial existente.
     *
     * @param entidad El objeto {@link CasoJudicial} con los nuevos datos.
     * @throws ErrorAlActualizarException si ocurre un error durante la
     * actualización.
     */
    @Override
    public void actualizar(CasoJudicial entidad) throws ErrorAlActualizarException {
        List<CasoJudicial> casos;

        try {
            casos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException("Caso Judicial", "No se pudo leer el archivo original: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (CasoJudicial c : casos) {
                if (c.getAsalto().getIdAsalto().equals(entidad.getAsalto().getIdAsalto())) {
                    bw.write(formatearParaArchivo(entidad));
                } else {
                    bw.write(formatearParaArchivo(c));
                }
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ErrorAlActualizarException("Caso Judicial", "No se pudo escribir en el archivo: " + e.getMessage());
        }
    }

    /**
     * Elimina un caso judicial basándose en el ID del asalto.
     *
     * @param id El ID del asalto asociado al caso a eliminar.
     * @throws ErrorAlEliminarException si ocurre un error durante la
     * eliminación.
     */
    @Override
    public void eliminar(String id) throws ErrorAlEliminarException {
        List<CasoJudicial> casos;

        try {
            casos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException("Caso Judicial", "No se pudo leer el archivo original: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (CasoJudicial c : casos) {
                if (!c.getAsalto().getIdAsalto().equals(id)) {
                    bw.write(formatearParaArchivo(c));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new ErrorAlEliminarException("Caso Judicial", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }
}
