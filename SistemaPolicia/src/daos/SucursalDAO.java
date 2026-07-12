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
import models.EntidadBancaria;
import models.Sucursal;

/**
 * Clase que implementa la persistencia de datos para la entidad
 * {@link Sucursal} utilizando un archivo de texto plano.
 */
public class SucursalDAO implements IGenericDAO<Sucursal> {

    // Ruta del archivo 
    private final String RUTA_ARCHIVO = "sucursales.txt";

    /**
     * Constructor que inicializa el DAO y asegura la existencia del archivo de
     * datos.
     */
    public SucursalDAO() {
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
            System.out.println("Error al crear el archivo de sucursales: " + e.getMessage());
        }
    }

    /**
     * Convierte un objeto Sucursal en una línea de texto CSV.
     *
     * @param sucursal La sucursal a formatear.
     * @return Una cadena de texto separada por comas.
     */
    private String formatearParaCSV(Sucursal sucursal) {
        return sucursal.getCodigo() + ","
                + sucursal.getDomicilio() + ","
                + sucursal.getNumeroEmpleados() + ","
                + sucursal.getEntidad().getCodigo();
    }

    /**
     * Verifica si ya existe una sucursal con el código proporcionado.
     *
     * @param codigo El código a buscar.
     * @return true si la sucursal existe, false en caso contrario.
     */
    private boolean existeSucursal(String codigo) {
        try {
            buscarPorId(codigo);
            return true;
        } catch (ObjetoNoEncontradoException | ErrorAlLeerException e) {
            return false;
        }
    }

    /**
     * Guarda una nueva sucursal al final del archivo de texto.
     *
     * * @param entidad La sucursal a persistir.
     * @throws ErrorAlGuardarException Si ocurre un error de E/S al escribir en
     * el archivo.
     */
    @Override
    public void guardar(Sucursal entidad) throws ErrorAlGuardarException {
        // Valido duplicados
        if (existeSucursal(entidad.getCodigo())) {
            throw new ErrorAlGuardarException("Sucursal", "El código " + entidad.getCodigo() + " ya existe.");
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            bw.write(formatearParaCSV(entidad));
            bw.newLine();
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Sucursal", e.getMessage());
        }
    }

    /**
     * Obtiene todas las sucursales almacenadas en el archivo.
     *
     * * @return Una {@link List} de objetos {@link Sucursal}.
     * @throws ErrorAlLeerException Si ocurre un error al acceder o leer el
     * archivo.
     */
    @Override
    public List<Sucursal> obtenerTodos() throws ErrorAlLeerException {
        List<Sucursal> listaSucursales = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
            String linea;

            // Leo línea por línea hasta que el archivo termine
            while ((linea = br.readLine()) != null) {

                // Separo los datos usando la coma
                String[] partes = linea.split(",");

                // Verifico que la línea tenga los 4 datos
                if (partes.length == 4) {
                    String codigo = partes[0];
                    String domicilio = partes[1];

                    // Parseo a texto el int para el número de empleados
                    int numeroEmpleados = Integer.parseInt(partes[2]);

                    String codigoEntidad = partes[3];

                    //Instancio un banco temporal con el código leído
                    EntidadBancaria entidad = new EntidadBancaria(codigoEntidad, "");

                    // Reconstruyo el objeto y agrego a la lista pasando la entidad
                    Sucursal sucursal = new Sucursal(codigo, domicilio, numeroEmpleados, entidad);
                    listaSucursales.add(sucursal);
                }
            }
        } catch (IOException e) {
            throw new ErrorAlLeerException("Archivo de Sucursales", e.getMessage());
        }
        return listaSucursales;
    }

    /**
     * Busca una sucursal específica por su código identificador.
     *
     * * @param id El código de la sucursal a buscar.
     * @return El objeto {@link Sucursal} encontrado.
     * @throws ObjetoNoEncontradoException Si no existe ninguna sucursal con
     * dicho ID.
     * @throws ErrorAlLeerException Si hay problemas de acceso al archivo.
     */
    @Override
    public Sucursal buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException {
        List<Sucursal> sucursales = obtenerTodos();
        for (Sucursal sucursal : sucursales) {
            // Busco usando el código de la sucursal como ID único
            if (sucursal.getCodigo().equals(id)) {
                return sucursal;
            }
        }
        throw new ObjetoNoEncontradoException("Sucursal", id);
    }

    /**
     * Actualiza los datos de una sucursal existente en el archivo.
     *
     * * @param entidad La sucursal con los datos actualizados.
     * @throws ErrorAlActualizarException Si ocurre un error durante el proceso
     * de sobrescritura.
     */
    @Override
    public void actualizar(Sucursal entidad) throws ErrorAlActualizarException {
        List<Sucursal> sucursales;

        // 1. Intento leer
        try {
            sucursales = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException("Sucursal", "No se pudo leer el archivo original: " + e.getMessage());
        }

        // 2. Intento escribir (sobrescribir)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Sucursal s : sucursales) {
                if (s.getCodigo().equals(entidad.getCodigo())) {
                    bw.write(entidad.getCodigo() + ","
                            + entidad.getDomicilio() + ","
                            + entidad.getNumeroEmpleados() + ","
                            + entidad.getEntidad().getCodigo());
                } else {
                    bw.write(s.getCodigo() + ","
                            + s.getDomicilio() + ","
                            + s.getNumeroEmpleados() + ","
                            + s.getEntidad().getCodigo());
                }
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ErrorAlActualizarException("Sucursal", "No se pudo escribir en el archivo: " + e.getMessage());
        }
    }

    /**
     * Elimina una sucursal del archivo según su código identificador.
     *
     * * @param id El código de la sucursal que se desea eliminar.
     * @throws ErrorAlEliminarException Si el proceso de escritura o lectura
     * falla.
     */
    @Override
    public void eliminar(String id) throws ErrorAlEliminarException {
        List<Sucursal> sucursales;

        // 1. Intento leer
        try {
            sucursales = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException("Sucursal", "No se pudo leer el archivo original: " + e.getMessage());
        }

        // 2. Intento escribir (sobrescribir omitiendo el id)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Sucursal s : sucursales) {
                if (!s.getCodigo().equals(id)) {
                    bw.write(s.getCodigo() + ","
                            + s.getDomicilio() + ","
                            + s.getNumeroEmpleados() + ","
                            + s.getEntidad().getCodigo());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new ErrorAlEliminarException("Sucursal", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }
}
