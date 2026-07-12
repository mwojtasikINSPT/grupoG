package daos;

import exceptions.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import models.Asaltante;
import models.Banda;

/**
 * Data Access Object para la gestión de entidades {@link Asaltante} en
 * persistencia de archivos. Implementa las operaciones CRUD básicas sobre el
 * archivo asaltantes.txt.
 */
public class AsaltanteDAO implements IGenericDAO<Asaltante> {

    private final String RUTA_ARCHIVO = "asaltantes.txt";

    /**
     * Constructor. Inicializa el DAO y asegura la existencia del archivo de
     * persistencia.
     */
    public AsaltanteDAO() {
        crearArchivoSiNoExiste();
    }

    /**
     * Verifica si el archivo de datos existe; en caso contrario, lo crea.
     */
    private void crearArchivoSiNoExiste() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Error al crear el archivo de asaltantes: " + e.getMessage());
        }
    }

    /**
     * Método auxiliar que centraliza el formato de texto para guardar en el
     * archivo CSV.
     *
     * @param a El asaltante a formatear.
     * @return String con los datos separados por comas.
     */
    private String formatearParaArchivo(Asaltante a) {
        return a.getClave() + ","
                + a.getNombreCompleto() + ","
                + (a.getBanda() != null ? a.getBanda().getNumeroBanda() : "null");
    }

    /**
     * Guarda un nuevo asaltante al final del archivo.
     *
     * @param entidad El objeto {@link Asaltante} a persistir.
     * @throws ErrorAlGuardarException si ocurre un error de E/S.
     */
    @Override
    public void guardar(Asaltante entidad) throws ErrorAlGuardarException {
        // Verifica si el asaltante ya existe para evitar duplicados
        if (existe(entidad.getClave())) {
            throw new ErrorAlGuardarException("Asaltante", "Ya existe un registro con la clave " + entidad.getClave());
        }

        //abre el archivo en modo "añadir" (true) 
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            //escribe los datos formateados
            bw.write(formatearParaArchivo(entidad));
            //salta a la siguiente línea, asegurando que el archivo se cierre al terminar.
            bw.newLine();
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Asaltante", e.getMessage());
        }
    }

    /**
     * Recupera todos los asaltantes almacenados en el archivo.
     *
     * @return Lista de objetos {@link Asaltante}.
     * @throws ErrorAlLeerException si ocurre un error de lectura.
     */
    @Override
    public List<Asaltante> obtenerTodos() throws ErrorAlLeerException {
        List<Asaltante> listaAsaltantes = new ArrayList<>();

        // Abre el archivo en modo lectura y asegura su cierre automático al finalizar
        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
            String linea;
            // Itera mientras haya contenido
            while ((linea = br.readLine()) != null) {
                // Divide la cadena de texto en un arreglo usando la coma como separador
                String[] partes = linea.split(",");
                // Valida que la línea tenga exactamente los 3 campos esperados para evitar errores
                if (partes.length == 3) {
                    // Reconstruye el objeto Banda (si el valor no es "null") para asociarlo al asaltante
                    Banda banda = partes[2].equals("null") ? null : new Banda(partes[2], 0);
                    // Crea el objeto Asaltante con los datos procesados y lo agrega a la lista
                    listaAsaltantes.add(new Asaltante(partes[0], partes[1], banda));
                }
            }
        } catch (IOException e) {
            throw new ErrorAlLeerException("Archivo de Asaltantes", e.getMessage());
        }
        return listaAsaltantes;
    }

    /**
     * Busca un asaltante por su clave única.
     *
     * @param id La clave (ID) del asaltante.
     * @return El {@link Asaltante} encontrado.
     * @throws ObjetoNoEncontradoException si no existe el ID.
     * @throws ErrorAlLeerException si falla la lectura del archivo.
     */
    @Override
    public Asaltante buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException {
        List<Asaltante> asaltantes = obtenerTodos();

        // Recorre cada objeto 'asaltante' dentro de la lista 'asaltantes'
        for (Asaltante asaltante : asaltantes) {
            // Compara la clave del asaltante actual con el ID que estamos buscando
            if (asaltante.getClave().equals(id)) {
                // Si coinciden, devuelve inmediatamente el objeto encontrado y termina la búsqueda
                return asaltante;
            }
        }
        throw new ObjetoNoEncontradoException("Asaltante", id);
    }

    /**
     * Actualiza los datos de un asaltante existente sobrescribiendo el archivo.
     *
     * @param entidad El {@link Asaltante} con los nuevos datos.
     * @throws ErrorAlActualizarException si ocurre un error de E/S.
     */
    @Override
    public void actualizar(Asaltante entidad) throws ErrorAlActualizarException {
        List<Asaltante> asaltantes;

        try {
            asaltantes = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException("Asaltante", "Error al leer para actualizar: " + e.getMessage());
        }

        // Abre el archivo en modo escritura (por defecto sobrescribe contenido anterior)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            // Recorre toda la lista de asaltantes cargada en memoria
            for (Asaltante a : asaltantes) {
                // Si el asaltante actual coincide con el que queremos actualizar, escribo el nuevo objeto
                if (a.getClave().equals(entidad.getClave())) {
                    bw.write(formatearParaArchivo(entidad));
                } else {
                    // Si no, escribimos el registro original sin cambios
                    bw.write(formatearParaArchivo(a));
                }
                // Agrega un salto de línea después de cada registro
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ErrorAlActualizarException("Asaltante", e.getMessage());
        }
    }

    /**
     * Elimina un asaltante del archivo por su clave única.
     *
     * @param id La clave del asaltante a eliminar.
     * @throws ErrorAlEliminarException si ocurre un error durante el proceso.
     */
    @Override
    public void eliminar(String id) throws ErrorAlEliminarException {
        List<Asaltante> asaltantes;
        try {
            asaltantes = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException("Asaltante", "Error al leer para eliminar: " + e.getMessage());
        }

        // Abre el archivo en modo escritura para sobrescribir su contenido actual
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            // Recorre la lista de asaltantes 
            for (Asaltante a : asaltantes) {
                // Verifica que la clave del asaltante NO coincida con el ID que queremos eliminar
                if (!a.getClave().equals(id)) {
                    // Si no coincide, conservamos el registro escribiéndolo en el archivo
                    //El que coincida, no se escribe, se pierde (Ver)
                    bw.write(formatearParaArchivo(a));
                    // Agrega un salto de línea para separar este registro del siguiente
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new ErrorAlEliminarException("Asaltante", e.getMessage());
        }
    }

    /**
     * Verifica si un asaltante existe buscando por su ID.
     *
     * @param id Clave del asaltante a verificar.
     * @return true si el asaltante existe, false en caso contrario.
     */
    public boolean existe(String id) {
        try {
            buscarPorId(id);
            return true; // Si no lanzó excepción, es que lo encontró.
        } catch (ObjetoNoEncontradoException | ErrorAlLeerException e) {
            return false; // Si lanzó excepción, es que no existe o hubo error al leer.
        }
    }
}
