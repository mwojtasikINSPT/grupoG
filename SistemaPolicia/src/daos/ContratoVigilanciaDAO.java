package daos;

import exceptions.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.ContratoVigilancia;
import models.Sucursal;
import models.Vigilante;

/**
 * Data Access Object para la gestión de entidades {@link ContratoVigilancia} en
 * persistencia de archivos. Implementa las operaciones CRUD básicas sobre el
 * archivo contratos_vigilancia.txt.
 */
public class ContratoVigilanciaDAO implements IGenericDAO<ContratoVigilancia> {

    private final String RUTA_ARCHIVO = "contratos_vigilancia.txt";

    /**
     * Constructor. Inicializa el DAO y asegura la existencia del archivo de
     * persistencia.
     */
    public ContratoVigilanciaDAO() {
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
            System.out.println("Error al crear el archivo de contratos: " + e.getMessage());
        }
    }

    /**
     * Genera un ID único compuesto por sucursal, vigilante y fecha para
     * facilitar búsquedas.
     *
     * @param c El contrato a procesar.
     * @return String con el formato ID compuesto.
     */
    private String generarIdCompuesto(ContratoVigilancia c) { //ver si sirve p busquedas
        return c.getSucursal().getCodigo() + "-" + c.getVigilante().getCodigo() + "-" + c.getFecha().toString();
    }

    /**
     * Método auxiliar que centraliza el formato de texto para guardar en el
     * archivo.
     *
     * @param c El contrato a formatear.
     * @return String con los datos separados por comas.
     */
    private String formatearParaArchivo(ContratoVigilancia c) {
        return c.getSucursal().getCodigo() + ","
                + c.getVigilante().getCodigo() + ","
                + c.getFecha().toString() + ","
                + c.isConArma();
    }

    /**
     * Verifica si un contrato existe en el sistema.
     *
     * @param id El ID compuesto del contrato.
     * @return true si el contrato existe, false en caso contrario.
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
     * Guarda un nuevo contrato en el archivo.
     *
     * @param entidad El objeto {@link ContratoVigilancia} a persistir.
     * @throws ErrorAlGuardarException si ocurre un error de escritura.
     */
    @Override
    public void guardar(ContratoVigilancia entidad) throws ErrorAlGuardarException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            bw.write(formatearParaArchivo(entidad));
            bw.newLine();
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Contrato Vigilancia", e.getMessage());
        }
    }

    /**
     * Recupera todos los contratos almacenados en el archivo.
     *
     * @return Lista de objetos {@link ContratoVigilancia}.
     * @throws ErrorAlLeerException si ocurre un error durante la lectura.
     */
    @Override
    public List<ContratoVigilancia> obtenerTodos() throws ErrorAlLeerException {
        List<ContratoVigilancia> listaContratos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");

                if (partes.length == 4) {
                    String codigoSucursal = partes[0];
                    String codigoVigilante = partes[1];
                    //Parseo fecha y  booleano del txt 
                    LocalDate fecha = LocalDate.parse(partes[2]);
                    boolean conArma = Boolean.parseBoolean(partes[3]);

                    //obj temporal con dato obtenido
                    Sucursal sucursal = new Sucursal(codigoSucursal, "", 0, null);
                    Vigilante vigilante = new Vigilante(codigoVigilante, 0);

                    ContratoVigilancia contrato = new ContratoVigilancia(sucursal, vigilante, fecha, conArma);
                    listaContratos.add(contrato);
                }
            }
        } catch (IOException e) {
            throw new ErrorAlLeerException("Archivo de Contratos", e.getMessage());
        }
        return listaContratos;
    }

    /**
     * Busca un contrato específico por su ID compuesto.
     *
     * @param id El identificador único generado.
     * @return El objeto {@link ContratoVigilancia} encontrado.
     * @throws ObjetoNoEncontradoException si no existe el contrato.
     * @throws ErrorAlLeerException si ocurre un error de lectura.
     */
    @Override
    public ContratoVigilancia buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException {
        List<ContratoVigilancia> contratos = obtenerTodos();
        for (ContratoVigilancia contrato : contratos) {
            // Comparo el ID compuesto que llegue con el generado 
            if (generarIdCompuesto(contrato).equals(id)) {
                return contrato;
            }
        }
        throw new ObjetoNoEncontradoException("Contrato Vigilancia", id);
    }

    /**
     * Actualiza un contrato existente.
     * @param entidad El contrato con los datos actualizados.
     * @throws ErrorAlActualizarException si ocurre un error de escritura.
     */
    @Override
    public void actualizar(ContratoVigilancia entidad) throws ErrorAlActualizarException {
        List<ContratoVigilancia> contratos;

        try {
            contratos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException("Contrato Vigilancia", "No se pudo leer el archivo original: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (ContratoVigilancia c : contratos) {
                if (generarIdCompuesto(c).equals(generarIdCompuesto(entidad))) {
                    bw.write(formatearParaArchivo(entidad));
                } else {
                    bw.write(formatearParaArchivo(c));
                }
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ErrorAlActualizarException("Contrato Vigilancia", "No se pudo escribir en el archivo: " + e.getMessage());
        }
    }

    /**
     * Elimina un contrato del sistema.
     * @param id El ID del contrato a eliminar.
     * @throws ErrorAlEliminarException si ocurre un error durante la eliminación.
     */
    @Override
    public void eliminar(String id) throws ErrorAlEliminarException {
        List<ContratoVigilancia> contratos;

        try {
            contratos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException("Contrato Vigilancia", "No se pudo leer el archivo original: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (ContratoVigilancia c : contratos) {
               if (!generarIdCompuesto(c).equals(id)) {
                    bw.write(formatearParaArchivo(c));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new ErrorAlEliminarException("Contrato Vigilancia", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }
}
