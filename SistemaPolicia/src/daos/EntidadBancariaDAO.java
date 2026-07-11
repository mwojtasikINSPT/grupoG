package daos;

import exceptions.ErrorAlActualizarException;
import exceptions.ErrorAlEliminarException;
import exceptions.ErrorAlGuardarException;
import exceptions.ErrorAlLeerException;
import exceptions.ObjetoNoEncontradoException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import models.EntidadBancaria;

public class EntidadBancariaDAO implements IGenericDAO<EntidadBancaria> {

    // Defino la ruta del archivo de texto
    private static final String RUTA_ARCHIVO = "entidades_bancarias.txt";

    @Override
    public void guardar(EntidadBancaria entidad) throws ErrorAlGuardarException {
        try {
            List<EntidadBancaria> entidades = obtenerTodos();
            entidades.add(entidad);
            reescribirArchivo(entidades);
        } catch (Exception e) {
            throw new ErrorAlGuardarException("Entidad Bancaria", e.getMessage());
        }
    }

    @Override
    public void actualizar(EntidadBancaria entidad) throws ErrorAlActualizarException {
        try {
            List<EntidadBancaria> entidades = obtenerTodos();
            boolean actualizado = false;

            for (int i = 0; i < entidades.size(); i++) {
                if (entidades.get(i).getCodigo().equals(entidad.getCodigo())) {
                    entidades.set(i, entidad);
                    actualizado = true;
                    break;
                }
            }

            if (actualizado) {
                // Si reescribirArchivo falla, cae al catch de abajo
                reescribirArchivo(entidades);
            } else {
                throw new Exception("No se encontró la entidad con código: " + entidad.getCodigo());
            }
        } catch (Exception e) {
            throw new ErrorAlActualizarException("Entidad Bancaria", e.getMessage());
        }
    }

    // --- MÉTODO AUX ---
    private void reescribirArchivo(List<EntidadBancaria> entidades) throws ErrorAlGuardarException {
        try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(RUTA_ARCHIVO))) {
            for (EntidadBancaria entidad : entidades) {
                bw.write(entidad.getCodigo() + "," + entidad.getDomicilioCentral());
                bw.newLine();
            }
        } catch (IOException e) {
            // Paso tipoObjeto y motivo 
            throw new ErrorAlGuardarException("Archivo de Entidades Bancarias", e.getMessage());
        }
    }

    @Override
    public List<EntidadBancaria> obtenerTodos() throws ErrorAlLeerException {
        List<EntidadBancaria> listaEntidades = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
            String linea;

            // Leo línea por línea hasta que el archivo termine
            while ((linea = br.readLine()) != null) {

                // Separo los datos usando la coma
                String[] partes = linea.split(",");

                // Verifico que la línea tenga los 2 datos (código y domicilio central)
                if (partes.length == 2) {
                    String codigo = partes[0];
                    String domicilio = partes[1];

                    // Reconstruyo el objeto y lo agrego a la lista
                    EntidadBancaria entidad = new EntidadBancaria(codigo, domicilio);
                    listaEntidades.add(entidad);
                }
            }
        } catch (IOException e) {
            throw new ErrorAlLeerException("Archivo de Entidades Bancarias", e.getMessage());
        }
        return listaEntidades;
    }

    @Override
    public EntidadBancaria buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException {
        List<EntidadBancaria> entidades = obtenerTodos();
        for (EntidadBancaria entidad : entidades) {
            // Busco usando el código del banco como ID único
            if (entidad.getCodigo().equals(id)) {
                return entidad;
            }
        }
        throw new ObjetoNoEncontradoException("Entidad Bancaria", id);
    }

    @Override
    public void eliminar(String id) throws ErrorAlEliminarException {
        try {
            List<EntidadBancaria> entidades = obtenerTodos();
            boolean removido = entidades.removeIf(entidad -> entidad.getCodigo().equals(id));

            if (removido) {
                reescribirArchivo(entidades);
            } else {
                throw new Exception("No se encontró la entidad con código: " + id);
            }
        } catch (Exception e) {
            throw new ErrorAlEliminarException("Entidad Bancaria", e.getMessage());
        }
    }
}
