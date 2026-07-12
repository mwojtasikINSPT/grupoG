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
import models.Vigilante;

/**
 * Clase que gestiona la persistencia de objetos {@link Vigilante} utilizando un
 * archivo de texto como almacenamiento.
 */
public class VigilanteDAO implements IGenericDAO<Vigilante> {

    // Ruta del archivo 
    private final String RUTA_ARCHIVO = "vigilantes.txt";

    /**
     * Constructor que inicializa el DAO y asegura la existencia del archivo de
     * datos.
     */
    public VigilanteDAO() {
        crearArchivoSiNoExiste();
    }

    /**
     * Verifica si el archivo de almacenamiento existe; en caso contrario, lo
     * crea.
     */
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

    /**
     * Formatea los datos de un vigilante para su almacenamiento en el archivo.
     *
     * * @param entidad El objeto {@link Vigilante} a formatear.
     * @return Una cadena de texto con el formato "código,edad".
     */
    private String armarLinea(Vigilante entidad) {
        return entidad.getCodigo() + "," + entidad.getEdad();
    }

    /**
     * Verifica si un vigilante existe en el archivo de almacenamiento.
     *
     * * @param codigo El código identificador del vigilante a buscar.
     * @return {@code true} si el vigilante existe, {@code false} en caso
     * contrario o si ocurrió un error al intentar realizar la lectura.
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
     * Almacena un objeto {@link Vigilante} en el archivo.
     *
     * * @param entidad El objeto {@link Vigilante} a persistir.
     * @throws ErrorAlGuardarException Si ocurre un error de E/S al escribir en
     * el archivo.
     */
    @Override
    public void guardar(Vigilante entidad) throws ErrorAlGuardarException {
        // Validamos duplicidad usando el método auxiliar
        if (existeVigilante(entidad.getCodigo())) {
            throw new ErrorAlGuardarException("Vigilante", "El vigilante con código " + entidad.getCodigo() + " ya existe.");
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            // Usamos el auxiliar para el formato de la línea
            bw.write(armarLinea(entidad));
            bw.newLine();
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Vigilante", e.getMessage());
        }
    }

    /**
     * Recupera todos los vigilantes persistidos en el archivo.
     *
     * * @return Una {@link List} que contiene todos los objetos
     * {@link Vigilante} encontrados.
     * @throws ErrorAlLeerException Si ocurre un error durante la lectura o
     * parseo del archivo.
     */
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

    /**
     * Busca un vigilante por su código identificador único.
     *
     * * @param id El código identificador del vigilante.
     * @return El objeto {@link Vigilante} encontrado.
     * @throws ObjetoNoEncontradoException Si no existe un vigilante con dicho
     * ID.
     * @throws ErrorAlLeerException Si hay un error de acceso al archivo.
     */
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

    /**
     * Actualiza los datos de un vigilante existente sobrescribiendo el registro
     * correspondiente.
     *
     * * @param entidad El objeto {@link Vigilante} con los datos actualizados.
     * @throws ErrorAlActualizarException Si el proceso de lectura o escritura
     * falla.
     */
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
                    //Vigilante actualizado
                    bw.write(armarLinea(entidad));
                } else {
                    //Queda como fue encontrado
                    bw.write(armarLinea(v));
                }
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ErrorAlActualizarException("Vigilante", "No se pudo escribir en el archivo: " + e.getMessage());
        }
    }

    /**
     * Elimina un vigilante del archivo basándose en su código identificador.
     * Lee la lista completa, filtra el vigilante cuyo código coincide con el id
     * proporcionado, y sobrescribe el archivo con los registros restantes.
     *
     * * @param id El código identificador del vigilante a eliminar.
     * @throws ErrorAlEliminarException Si ocurre un error al leer el archivo
     * original o durante el proceso de escritura.
     */
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
                    // Solo escribo los registros que NO coinciden con el ID
                    bw.write(armarLinea(v));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new ErrorAlEliminarException("Vigilante", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }
}
