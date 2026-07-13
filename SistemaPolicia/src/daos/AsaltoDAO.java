package daos;

import exceptions.*;
import exceptions.ObjetoNoEncontradoException;
import java.io.BufferedReader; // Lee texto de forma rápida y eficiente (línea por línea) usando memoria temporal.
import java.io.BufferedWriter; // Escribe texto de forma eficiente acumulándolo en memoria antes de pasarlo al disco.
import java.io.File;           // Representa la ruta o la existencia del archivo físico en el disco duro.
import java.io.FileReader;     // Abre la conexión directa para leer los caracteres del archivo.
import java.io.FileWriter;     // Abre la conexión directa para escribir caracteres dentro del archivo.
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.Asaltante;
import models.Asalto;
import models.Sucursal;

/**
 * Data Access Object para la gestión de entidades {@link Asalto} en
 * persistencia de archivos. Implementa las operaciones CRUD básicas sobre el
 * archivo asaltos.txt.
 */
public class AsaltoDAO implements IGenericDAO<Asalto> {

    /**
     * Instancia para acceder a los datos de asaltantes.
     */
    private final AsaltanteDAO asaltanteDAO;

    /**
     * Instancia para acceder a los datos de sucursales.
     */
    private final SucursalDAO sucursalDAO;

    // Ruta del archivo 
    private final String RUTA_ARCHIVO = "asaltos.txt";

    public AsaltoDAO() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
            System.out.println("Error al crear el archivo de asaltos: " + e.getMessage());
        }
    }

    /**
     * Construye un nuevo AsaltoDAO inyectando las dependencias necesarias para
     * la gestión de relaciones.
     *
     * @param asaltanteDAO El DAO encargado de los asaltantes.
     * @param sucursalDAO El DAO encargado de las sucursales.
     */
    public AsaltoDAO(AsaltanteDAO asaltanteDAO, SucursalDAO sucursalDAO) {
        this.asaltanteDAO = asaltanteDAO;
        this.sucursalDAO = sucursalDAO;
        crearArchivoSiNoExiste();
    }

    /**
     * Método auxiliar que centraliza el formato de texto para guardar en el
     * archivo.
     */
    private String formatearParaArchivo(Asalto a) {
        return a.getIdAsalto() + ","
                + a.getAsaltante().getClave() + ","
                + a.getSucursal().getCodigo() + ","
                + a.getFecha().toString();
    }

    /**
     * Verifica si un asalto existe en el sistema.
     *
     * @param id ID del asalto.
     * @return true si existe, false si no.
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
     * Guarda un nuevo asalto en el archivo, validando duplicados.
     *
     * @param entidad El objeto {@link Asalto} a persistir.
     * @throws ErrorAlGuardarException si ocurre un error de E/S o el ID ya
     * existe.
     */
    @Override
    public void guardar(Asalto entidad) throws ErrorAlGuardarException {
        if (existe(entidad.getIdAsalto())) {
            throw new ErrorAlGuardarException("Asalto", "Ya existe un asalto con ID " + entidad.getIdAsalto());
        }

        //abre el archivo en modo "añadir" (true) 
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            //escribe los datos formateados
            bw.write(formatearParaArchivo(entidad));
            //salta a la siguiente línea, asegurando que el archivo se cierre al terminar.
            bw.newLine();
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Asalto", e.getMessage());
        }
    }

    /**
     * Recupera todos los asaltos almacenados en el archivo.
     *
     * @return Lista de objetos {@link Asalto}.
     * @throws ErrorAlLeerException si ocurre un error durante la lectura.
     */
    @Override
    public List<Asalto> obtenerTodos() throws ErrorAlLeerException {
        List<Asalto> listaAsaltos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");

                // El modelo tiene 4 atributos, verifico que haya 4 partes
                if (partes.length == 4) {
                    String idAsalto = partes[0];
                    String claveAsaltante = partes[1];
                    String codigoSucursal = partes[2];
                    // Convierto el texto nuevamente a LocalDate
                    LocalDate fecha = LocalDate.parse(partes[3]);

                    // Buscamos en el DAO correspondiente
                    Asaltante asaltante = asaltanteDAO.buscarPorId(claveAsaltante);
                    Sucursal sucursal = sucursalDAO.buscarPorId(codigoSucursal);

                    Asalto asalto = new Asalto(idAsalto, asaltante, sucursal, fecha);
                    listaAsaltos.add(asalto);
                }
            }
        } catch (IOException e) {
            throw new ErrorAlLeerException("Archivo de Asaltos", e.getMessage());
        } catch (ObjetoNoEncontradoException ex) {
            System.getLogger(AsaltoDAO.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            //VER
        }
        return listaAsaltos;
    }

    /**
     * Busca un asalto específico por su ID único.
     *
     * @param id El identificador del asalto.
     * @return El objeto {@link Asalto} encontrado.
     * @throws ObjetoNoEncontradoException si no existe el ID.
     * @throws ErrorAlLeerException si ocurre un error de lectura.
     */
    @Override
    public Asalto buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException {
        List<Asalto> asaltos = obtenerTodos();
        for (Asalto asalto : asaltos) {
            if (asalto.getIdAsalto().equals(id)) {
                return asalto;
            }
        }
        throw new ObjetoNoEncontradoException("Asalto", id);
    }

    /**
     * Actualiza un asalto existente sobrescribiendo el archivo.
     *
     * @param entidad El objeto {@link Asalto} actualizado.
     * @throws ErrorAlActualizarException si ocurre un error durante la
     * escritura.
     */
    @Override
    public void actualizar(Asalto entidad) throws ErrorAlActualizarException {
        List<Asalto> asaltos;

        try {
            asaltos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException("Asalto", "No se pudo leer el archivo original: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Asalto a : asaltos) {
                if (a.getIdAsalto().equals(entidad.getIdAsalto())) {
                    bw.write(formatearParaArchivo(entidad));
                } else {
                    bw.write(formatearParaArchivo(a));
                }
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ErrorAlActualizarException("Asalto", "No se pudo escribir en el archivo: " + e.getMessage());
        }
    }

    /**
     * Elimina un asalto del registro mediante su ID.
     *
     * @param id El identificador del asalto a borrar.
     * @throws ErrorAlEliminarException si ocurre un error durante la
     * eliminación.
     */
    @Override
    public void eliminar(String id) throws ErrorAlEliminarException {
        List<Asalto> asaltos;

        try {
            asaltos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException("Asalto", "No se pudo leer el archivo original: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Asalto a : asaltos) {
                if (!a.getIdAsalto().equals(id)) {
                    bw.write(formatearParaArchivo(a));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new ErrorAlEliminarException("Asalto", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }
}
