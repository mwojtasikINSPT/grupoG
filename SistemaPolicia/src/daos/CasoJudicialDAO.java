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
import models.CasoJudicial;

/**
 * Gestiona la persistencia de casos judiciales mediante un archivo de texto.
 *
 * Cada registro almacena los identificadores del asalto y del juez, junto con
 * el resultado del proceso judicial.
 *
 * @author GrupoG
 */
public class CasoJudicialDAO implements ICasoJudicialDAO {

    private static final String RUTA_ARCHIVO =
            "casos_judiciales.txt";

    /**
     * Crea el DAO y comprueba que exista el archivo de casos judiciales.
     */
    public CasoJudicialDAO() {
        crearArchivoSiNoExiste();
    }

    /**
     * Crea el archivo de casos judiciales cuando todavía no existe.
     */
    private void crearArchivoSiNoExiste() {
        try {
            File archivo = new File(RUTA_ARCHIVO);

            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            System.out.println(
                    "Error al crear el archivo de casos judiciales: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Convierte un caso judicial en una línea de texto separada por comas.
     *
     * @param caso caso que se convertirá
     * @return línea preparada para almacenarse
     */
    private String formatearParaArchivo(CasoJudicial caso) {
        return caso.getIdAsalto() + ","
                + caso.getIdJuez() + ","
                + caso.isCondenado() + ","
                + caso.getMesesCarcel();
    }

    /**
     * Comprueba si existe un caso asociado con el asalto indicado.
     *
     * @param idAsalto identificador del asalto
     * @return {@code true} si el caso existe
     */
    @Override
    public boolean existe(String idAsalto) {
        try {
            buscarPorId(idAsalto);
            return true;
        } catch (ObjetoNoEncontradoException | ErrorAlLeerException e) {
            return false;
        }
    }

    /**
     * Guarda un caso judicial si el asalto todavía no tiene uno registrado.
     *
     * @param caso caso judicial que se guardará
     * @throws ErrorAlGuardarException si faltan datos, el caso ya existe o no
     * puede escribirse el archivo
     */
    @Override
    public void guardar(CasoJudicial caso)
            throws ErrorAlGuardarException {

        validarDatos(caso);

        if (existe(caso.getIdAsalto())) {
            throw new ErrorAlGuardarException(
                    "Caso Judicial",
                    "Ya existe un caso para el asalto ID "
                    + caso.getIdAsalto()
            );
        }

        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(RUTA_ARCHIVO, true))) {

            escritor.write(formatearParaArchivo(caso));
            escritor.newLine();

        } catch (IOException e) {
            throw new ErrorAlGuardarException(
                    "Caso Judicial",
                    e.getMessage()
            );
        }
    }

    /**
     * Recupera todos los casos judiciales almacenados.
     *
     * Los modelos se reconstruyen directamente con los identificadores
     * guardados, sin consultar ni crear objetos completos relacionados.
     *
     * @return lista de casos judiciales
     * @throws ErrorAlLeerException si el archivo no puede leerse o contiene
     * una cantidad de meses inválida
     */
    @Override
    public List<CasoJudicial> obtenerTodos()
            throws ErrorAlLeerException {

        List<CasoJudicial> casos = new ArrayList<>();

        try (BufferedReader lector = new BufferedReader(
                new FileReader(RUTA_ARCHIVO))) {

            String linea;

            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(",");

                if (partes.length != 4) {
                    continue;
                }

                String idAsalto = partes[0];
                String idJuez = partes[1];
                boolean condenado =
                        Boolean.parseBoolean(partes[2]);

                int mesesCarcel =
                        Integer.parseInt(partes[3]);

                casos.add(
                        new CasoJudicial(
                                idAsalto,
                                idJuez,
                                condenado,
                                mesesCarcel
                        )
                );
            }

        } catch (IOException e) {
            throw new ErrorAlLeerException(
                    "Archivo de Casos Judiciales",
                    e.getMessage()
            );

        } catch (NumberFormatException e) {
            throw new ErrorAlLeerException(
                    "Archivo de Casos Judiciales",
                    "Cantidad de meses inválida: "
                    + e.getMessage()
            );
        }

        return casos;
    }

    /**
     * Busca un caso por el identificador de su asalto.
     *
     * @param id identificador del asalto
     * @return caso judicial encontrado
     * @throws ObjetoNoEncontradoException si el caso no existe
     * @throws ErrorAlLeerException si no puede leerse el archivo
     */
    @Override
    public CasoJudicial buscarPorId(String id)
            throws ObjetoNoEncontradoException, ErrorAlLeerException {

        List<CasoJudicial> casos = obtenerTodos();

        for (CasoJudicial caso : casos) {
            if (caso.getIdAsalto().equals(id)) {
                return caso;
            }
        }

        throw new ObjetoNoEncontradoException(
                "Caso Judicial",
                id
        );
    }

    /**
     * Actualiza los datos de un caso judicial existente.
     *
     * @param caso caso que contiene los nuevos datos
     * @throws ErrorAlActualizarException si el archivo no puede leerse o
     * reescribirse
     */
    @Override
    public void actualizar(CasoJudicial caso)
            throws ErrorAlActualizarException {

        List<CasoJudicial> casos;

        try {
            casos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException(
                    "Caso Judicial",
                    "No se pudo leer el archivo original: "
                    + e.getMessage()
            );
        }

        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(RUTA_ARCHIVO))) {

            for (CasoJudicial casoGuardado : casos) {
                if (casoGuardado.getIdAsalto()
                        .equals(caso.getIdAsalto())) {

                    escritor.write(formatearParaArchivo(caso));
                } else {
                    escritor.write(
                            formatearParaArchivo(casoGuardado)
                    );
                }

                escritor.newLine();
            }

        } catch (IOException e) {
            throw new ErrorAlActualizarException(
                    "Caso Judicial",
                    "No se pudo escribir en el archivo: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Elimina el caso asociado con el asalto indicado.
     *
     * @param id identificador del asalto
     * @throws ErrorAlEliminarException si el archivo no puede leerse o
     * reescribirse
     */
    @Override
    public void eliminar(String id)
            throws ErrorAlEliminarException {

        List<CasoJudicial> casos;

        try {
            casos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException(
                    "Caso Judicial",
                    "No se pudo leer el archivo original: "
                    + e.getMessage()
            );
        }

        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(RUTA_ARCHIVO))) {

            for (CasoJudicial caso : casos) {
                if (!caso.getIdAsalto().equals(id)) {
                    escritor.write(formatearParaArchivo(caso));
                    escritor.newLine();
                }
            }

        } catch (IOException e) {
            throw new ErrorAlEliminarException(
                    "Caso Judicial",
                    "No se pudo eliminar el registro: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Comprueba que el caso contenga los datos obligatorios.
     *
     * @param caso caso que se validará
     * @throws ErrorAlGuardarException si falta algún dato
     */
    private void validarDatos(CasoJudicial caso)
            throws ErrorAlGuardarException {

        if (caso == null
                || caso.getIdAsalto() == null
                || caso.getIdAsalto().trim().isEmpty()
                || caso.getIdJuez() == null
                || caso.getIdJuez().trim().isEmpty()) {

            throw new ErrorAlGuardarException(
                    "Caso Judicial",
                    "Faltan datos obligatorios."
            );
        }
    }
}