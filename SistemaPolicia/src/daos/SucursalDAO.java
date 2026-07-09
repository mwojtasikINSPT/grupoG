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
import models.EntidadBancaria;
import models.Sucursal;

public class SucursalDAO implements IGenericDAO<Sucursal> {

    // Ruta del archivo 
    private final String RUTA_ARCHIVO = "sucursales.txt";

    public SucursalDAO() {
        crearArchivoSiNoExiste();
    }

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

    @Override
    public void guardar(Sucursal entidad) throws ErrorAlGuardarException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {

            // Armo la línea separada por comas
            String linea = entidad.getCodigo() + ","
                    + entidad.getDomicilio() + ","
                    + entidad.getNumeroEmpleados() + ","
                    + entidad.getEntidad().getCodigo();

            bw.write(linea);
            bw.newLine();
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Sucursal", e.getMessage());
        }
    }

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
