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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import models.Asalto;

/**
 * Gestiona la persistencia de asaltos mediante un archivo de texto.
 *
 * Cada registro almacena únicamente el identificador del asalto, los IDs de
 * las entidades relacionadas y la fecha del hecho.
 *
 * @author GrupoG
 */
public class AsaltoDAO implements IAsaltoDAO {

    private static final String RUTA_ARCHIVO = "asaltos.txt";

    /**
     * Crea el DAO y comprueba que exista el archivo de asaltos.
     */
    public AsaltoDAO() {
        crearArchivoSiNoExiste();
    }

    /**
     * Crea el archivo de asaltos cuando todavía no existe.
     */
    private void crearArchivoSiNoExiste() {
        try {
            File archivo = new File(RUTA_ARCHIVO);

            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            System.out.println(
                    "Error al crear el archivo de asaltos: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Convierte un asalto en una línea de texto separada por comas.
     *
     * @param asalto hecho que se convertirá
     * @return línea preparada para almacenarse
     */
    private String formatearParaArchivo(Asalto asalto) {
        return asalto.getIdAsalto() + ","
                + asalto.getIdAsaltante() + ","
                + asalto.getIdSucursal() + ","
                + asalto.getFecha();
    }

    /**
     * Comprueba si existe un asalto con el identificador indicado.
     *
     * @param id identificador del asalto
     * @return {@code true} si el asalto existe
     */
    @Override
    public boolean existe(String id) {
        try {
            buscarPorId(id);
            return true;
        } catch (ObjetoNoEncontradoException | ErrorAlLeerException e) {
            return false;
        }
    }

    /**
     * Guarda un asalto si su identificador todavía no está registrado.
     *
     * @param asalto asalto que se guardará
     * @throws ErrorAlGuardarException si faltan datos, el identificador ya
     * existe o no puede escribirse el archivo
     */
    @Override
    public void guardar(Asalto asalto)
            throws ErrorAlGuardarException {

        validarDatos(asalto);

        if (existe(asalto.getIdAsalto())) {
            throw new ErrorAlGuardarException(
                    "Asalto",
                    "Ya existe un asalto con ID "
                    + asalto.getIdAsalto()
            );
        }

        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(RUTA_ARCHIVO, true))) {

            escritor.write(formatearParaArchivo(asalto));
            escritor.newLine();

        } catch (IOException e) {
            throw new ErrorAlGuardarException(
                    "Asalto",
                    e.getMessage()
            );
        }
    }

    /**
     * Recupera todos los asaltos almacenados.
     *
     * Las relaciones se reconstruyen mediante sus identificadores, sin
     * consultar ni crear objetos completos de asaltantes o sucursales.
     *
     * @return lista de asaltos almacenados
     * @throws ErrorAlLeerException si el archivo no puede leerse o contiene
     * una fecha inválida
     */
    @Override
    public List<Asalto> obtenerTodos()
            throws ErrorAlLeerException {

        List<Asalto> asaltos = new ArrayList<>();

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
                String idAsaltante = partes[1];
                String idSucursal = partes[2];
                LocalDate fecha = LocalDate.parse(partes[3]);

                asaltos.add(
                        new Asalto(
                                idAsalto,
                                idAsaltante,
                                idSucursal,
                                fecha
                        )
                );
            }

        } catch (IOException e) {
            throw new ErrorAlLeerException(
                    "Archivo de Asaltos",
                    e.getMessage()
            );

        } catch (DateTimeParseException e) {
            throw new ErrorAlLeerException(
                    "Archivo de Asaltos",
                    "Existe una fecha inválida: "
                    + e.getMessage()
            );
        }

        return asaltos;
    }

    /**
     * Busca un asalto por su identificador.
     *
     * @param id identificador del asalto buscado
     * @return asalto encontrado
     * @throws ObjetoNoEncontradoException si el asalto no existe
     * @throws ErrorAlLeerException si no puede leerse el archivo
     */
    @Override
    public Asalto buscarPorId(String id)
            throws ObjetoNoEncontradoException, ErrorAlLeerException {

        List<Asalto> asaltos = obtenerTodos();

        for (Asalto asalto : asaltos) {
            if (asalto.getIdAsalto().equals(id)) {
                return asalto;
            }
        }

        throw new ObjetoNoEncontradoException("Asalto", id);
    }

    /**
     * Actualiza los datos de un asalto existente.
     *
     * @param asalto asalto que contiene los nuevos datos
     * @throws ErrorAlActualizarException si el archivo no puede leerse o
     * reescribirse
     */
    @Override
    public void actualizar(Asalto asalto)
            throws ErrorAlActualizarException {

        List<Asalto> asaltos;

        try {
            asaltos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException(
                    "Asalto",
                    "No se pudo leer el archivo original: "
                    + e.getMessage()
            );
        }

        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(RUTA_ARCHIVO))) {

            for (Asalto asaltoGuardado : asaltos) {
                if (asaltoGuardado.getIdAsalto()
                        .equals(asalto.getIdAsalto())) {

                    escritor.write(formatearParaArchivo(asalto));
                } else {
                    escritor.write(
                            formatearParaArchivo(asaltoGuardado)
                    );
                }

                escritor.newLine();
            }

        } catch (IOException e) {
            throw new ErrorAlActualizarException(
                    "Asalto",
                    "No se pudo escribir en el archivo: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Elimina el asalto que tenga el identificador indicado.
     *
     * @param id identificador del asalto que se eliminará
     * @throws ErrorAlEliminarException si el archivo no puede leerse o
     * reescribirse
     */
    @Override
    public void eliminar(String id)
            throws ErrorAlEliminarException {

        List<Asalto> asaltos;

        try {
            asaltos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException(
                    "Asalto",
                    "No se pudo leer el archivo original: "
                    + e.getMessage()
            );
        }

        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(RUTA_ARCHIVO))) {

            for (Asalto asalto : asaltos) {
                if (!asalto.getIdAsalto().equals(id)) {
                    escritor.write(formatearParaArchivo(asalto));
                    escritor.newLine();
                }
            }

        } catch (IOException e) {
            throw new ErrorAlEliminarException(
                    "Asalto",
                    "No se pudo eliminar el registro: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Comprueba que el asalto contenga los datos obligatorios.
     *
     * @param asalto asalto que se validará
     * @throws ErrorAlGuardarException si falta algún dato
     */
    private void validarDatos(Asalto asalto)
            throws ErrorAlGuardarException {

        if (asalto == null
                || asalto.getIdAsalto() == null
                || asalto.getIdAsalto().trim().isEmpty()
                || asalto.getIdAsaltante() == null
                || asalto.getIdAsaltante().trim().isEmpty()
                || asalto.getIdSucursal() == null
                || asalto.getIdSucursal().trim().isEmpty()
                || asalto.getFecha() == null) {

            throw new ErrorAlGuardarException(
                    "Asalto",
                    "Faltan datos obligatorios."
            );
        }
    }
}