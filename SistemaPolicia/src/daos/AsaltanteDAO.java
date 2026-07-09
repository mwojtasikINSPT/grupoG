package daos;

import exceptions.ErrorAlActualizarException;
import exceptions.ErrorAlEliminarException;
import exceptions.ErrorAlGuardarException;
import exceptions.ErrorAlLeerException;
import exceptions.ObjetoNoEncontradoException;
import java.io.BufferedReader; // Lee texto de forma rápida y eficiente (línea por línea) usando memoria temporal.
import java.io.BufferedWriter; // Escribe texto de forma eficiente acumulándolo en memoria antes de pasarlo al disco.
import java.io.File;           // Representa la ruta o la existencia del archivo físico en el disco duro.
import java.io.FileReader;     // Abre la conexión directa para leer los caracteres del archivo.
import java.io.FileWriter;     // Abre la conexión directa para escribir caracteres dentro del archivo.
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import models.Asaltante;
import models.Banda;

public class AsaltanteDAO implements IGenericDAO<Asaltante> {

    // Ruta del archivo 
    private final String RUTA_ARCHIVO = "asaltantes.txt";

    public AsaltanteDAO() {
        crearArchivoSiNoExiste();
    }

    private void crearArchivoSiNoExiste() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error al crear el archivo de asaltantes: " + e.getMessage());
        }
    }

    @Override
    public void guardar(Asaltante entidad) throws ErrorAlGuardarException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {

            // Armo la línea separada por comas
            String linea = entidad.getClave() + ","
                    + entidad.getNombreCompleto() + ","
                    + (entidad.getBanda() != null ? entidad.getBanda().getNumeroBanda() : "null");

            bw.write(linea);
            bw.newLine();
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Asaltante", e.getMessage());
        }
    }

    @Override
    public List<Asaltante> obtenerTodos() throws ErrorAlLeerException {
        List<Asaltante> listaAsaltantes = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
            String linea;

            // Leo línea por línea hasta que el archivo termine
            while ((linea = br.readLine()) != null) {

                // Separo los datos usando la coma
                String[] partes = linea.split(",");

                // Verifico que la línea tenga  los 3 datos
                if (partes.length == 3) {
                    String clave = partes[0];
                    String nombreCompleto = partes[1];
                    String numeroBanda = partes[2];

                    Banda banda = numeroBanda.equals("null") ? null : new Banda(numeroBanda, 0);

                    // Reconstruyo el objeto y lo agrego a la lista
                    Asaltante asaltante = new Asaltante(clave, nombreCompleto, banda);
                    listaAsaltantes.add(asaltante);
                }
            }
        } catch (IOException e) {
            throw new ErrorAlLeerException("Archivo de Asaltantes", e.getMessage());
        }
        return listaAsaltantes;
    }

    @Override
    public Asaltante buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException {
        List<Asaltante> asaltantes = obtenerTodos();
        for (Asaltante asaltante : asaltantes) {
            // Busco usando la clave como ID único
            if (asaltante.getClave().equals(id)) {
                return asaltante;
            }
        }
        throw new ObjetoNoEncontradoException("Asaltante", id);
    }

    @Override
    public void actualizar(Asaltante entidad) throws ErrorAlActualizarException {
        List<Asaltante> asaltantes;

        // 1. Intento leer
        try {
            asaltantes = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException("Asaltante", "No se pudo leer el archivo original: " + e.getMessage());
        }

        // 2. Intento escribir (sobrescribir)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Asaltante a : asaltantes) {
                if (a.getClave().equals(entidad.getClave())) {
                    bw.write(entidad.getClave() + ","
                            + entidad.getNombreCompleto() + ","
                            + (entidad.getBanda() != null ? entidad.getBanda().getNumeroBanda() : "null"));
                } else {
                    bw.write(a.getClave() + ","
                            + a.getNombreCompleto() + ","
                            + (a.getBanda() != null ? a.getBanda().getNumeroBanda() : "null"));
                }
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ErrorAlActualizarException("Asaltante", "No se pudo escribir en el archivo: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(String id) throws ErrorAlEliminarException {
        List<Asaltante> asaltantes;

        // 1. Intento leer
        try {
            asaltantes = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException("Asaltante", "No se pudo leer el archivo original: " + e.getMessage());
        }

        // 2. Intento escribir (sobrescribir omitiendo el id)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Asaltante a : asaltantes) {
                if (!a.getClave().equals(id)) {
                    bw.write(a.getClave() + ","
                            + a.getNombreCompleto() + ","
                            + (a.getBanda() != null ? a.getBanda().getNumeroBanda() : "null"));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new ErrorAlEliminarException("Asaltante", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }
}
