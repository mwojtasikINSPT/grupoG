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

/**
 * Gestiona la persistencia de vigilantes mediante un archivo de texto.
 *
 * Implementa {@link IGenericDAO} para que posteriormente pueda reemplazarse
 * por una implementación que utilice MySQL.
 *
 * @author GrupoG
 */
public class VigilanteDAO
        implements IGenericDAO<Vigilante> {

    private static final String RUTA_ARCHIVO = "vigilantes.txt";

    /**
     * Crea el DAO y comprueba que exista el archivo de vigilantes.
     */
    public VigilanteDAO() {
        crearArchivoSiNoExiste();
    }

    /**
     * Crea el archivo de vigilantes cuando todavía no existe.
     */
    private void crearArchivoSiNoExiste() {
        try {
            File archivo = new File(RUTA_ARCHIVO);

            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            System.out.println(
                    "Error al crear el archivo de vigilantes: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Convierte un vigilante en una línea de texto separada por comas.
     *
     * @param vigilante vigilante que se convertirá
     * @return línea con el código y la edad
     */
    private String armarLinea(Vigilante vigilante) {
        return vigilante.getCodigo()
                + ","
                + vigilante.getEdad();
    }

    /**
     * Comprueba si existe un vigilante con el código indicado.
     *
     * @param codigo código del vigilante buscado
     * @return {@code true} si el vigilante existe
     */
    private boolean existeVigilante(String codigo) {
        try {
            buscarPorId(codigo);
            return true;
        } catch (ObjetoNoEncontradoException | ErrorAlLeerException e) {
            return false;
        }
    }

    /**
     * Guarda un vigilante si su código todavía no está registrado.
     *
     * @param vigilante vigilante que se guardará
     * @throws ErrorAlGuardarException si el vigilante ya existe o no puede
     * escribirse el archivo
     */
    @Override
    public void guardar(Vigilante vigilante)
            throws ErrorAlGuardarException {

        if (existeVigilante(vigilante.getCodigo())) {
            throw new ErrorAlGuardarException(
                    "Vigilante",
                    "El vigilante con código "
                    + vigilante.getCodigo()
                    + " ya existe."
            );
        }

        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(RUTA_ARCHIVO, true))) {

            escritor.write(armarLinea(vigilante));
            escritor.newLine();

        } catch (IOException e) {
            throw new ErrorAlGuardarException(
                    "Vigilante",
                    e.getMessage()
            );
        }
    }

    /**
     * Recupera todos los vigilantes almacenados.
     *
     * Las líneas incompletas se ignoran para evitar que interrumpan la lectura
     * del resto del archivo.
     *
     * @return lista de vigilantes almacenados
     * @throws ErrorAlLeerException si no puede leerse o interpretarse el
     * archivo
     */
    @Override
    public List<Vigilante> obtenerTodos()
            throws ErrorAlLeerException {

        List<Vigilante> vigilantes = new ArrayList<>();

        try (BufferedReader lector = new BufferedReader(
                new FileReader(RUTA_ARCHIVO))) {

            String linea;

            while ((linea = lector.readLine()) != null) {
                String[] partes = linea.split(",");

                if (partes.length != 2) {
                    continue;
                }

                String codigo = partes[0];
                int edad = Integer.parseInt(partes[1]);

                vigilantes.add(new Vigilante(codigo, edad));
            }

        } catch (IOException | NumberFormatException e) {
            throw new ErrorAlLeerException(
                    "Archivo de Vigilantes",
                    e.getMessage()
            );
        }

        return vigilantes;
    }

    /**
     * Busca un vigilante por su código.
     *
     * @param id código del vigilante buscado
     * @return vigilante encontrado
     * @throws ObjetoNoEncontradoException si el vigilante no existe
     * @throws ErrorAlLeerException si no puede leerse el archivo
     */
    @Override
    public Vigilante buscarPorId(String id)
            throws ObjetoNoEncontradoException, ErrorAlLeerException {

        List<Vigilante> vigilantes = obtenerTodos();

        for (Vigilante vigilante : vigilantes) {
            if (vigilante.getCodigo().equals(id)) {
                return vigilante;
            }
        }

        throw new ObjetoNoEncontradoException("Vigilante", id);
    }

    /**
     * Actualiza los datos del vigilante que tenga el mismo código.
     *
     * @param vigilante vigilante que contiene los nuevos datos
     * @throws ErrorAlActualizarException si el archivo no puede leerse o
     * reescribirse
     */
    @Override
    public void actualizar(Vigilante vigilante)
            throws ErrorAlActualizarException {

        List<Vigilante> vigilantes;

        try {
            vigilantes = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException(
                    "Vigilante",
                    "No se pudo leer el archivo original: "
                    + e.getMessage()
            );
        }

        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(RUTA_ARCHIVO))) {

            for (Vigilante vigilanteGuardado : vigilantes) {
                if (vigilanteGuardado.getCodigo()
                        .equals(vigilante.getCodigo())) {

                    escritor.write(armarLinea(vigilante));
                } else {
                    escritor.write(armarLinea(vigilanteGuardado));
                }

                escritor.newLine();
            }

        } catch (IOException e) {
            throw new ErrorAlActualizarException(
                    "Vigilante",
                    "No se pudo escribir en el archivo: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Elimina el vigilante que tenga el código indicado.
     *
     * @param id código del vigilante que se eliminará
     * @throws ErrorAlEliminarException si el archivo no puede leerse o
     * reescribirse
     */
    @Override
    public void eliminar(String id)
            throws ErrorAlEliminarException {

        List<Vigilante> vigilantes;

        try {
            vigilantes = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException(
                    "Vigilante",
                    "No se pudo leer el archivo original: "
                    + e.getMessage()
            );
        }

        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(RUTA_ARCHIVO))) {

            for (Vigilante vigilante : vigilantes) {
                if (!vigilante.getCodigo().equals(id)) {
                    escritor.write(armarLinea(vigilante));
                    escritor.newLine();
                }
            }

        } catch (IOException e) {
            throw new ErrorAlEliminarException(
                    "Vigilante",
                    "No se pudo eliminar el registro: "
                    + e.getMessage()
            );
        }
    }
}