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
import models.CasoJudicial;

public class CasoJudicialDAO implements IGenericDAO<CasoJudicial> {

    // Ruta del archivo 
    private final String RUTA_ARCHIVO = "casos_judiciales.txt";

    public CasoJudicialDAO() {
        crearArchivoSiNoExiste();
    }

    private void crearArchivoSiNoExiste() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error al crear el archivo de casos judiciales: " + e.getMessage());
        }
    }

    @Override
    public void guardar(CasoJudicial entidad) throws ErrorAlGuardarException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            String linea = entidad.getIdAsalto() + "," + 
                           entidad.getClaveJuez() + "," + 
                           entidad.isCondenado() + "," + 
                           entidad.getMesesCarcel();
            
            bw.write(linea);
            bw.newLine(); 
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Caso Judicial", e.getMessage());
        }
    }

    @Override
    public List<CasoJudicial> obtenerTodos() throws ErrorAlLeerException {
        List<CasoJudicial> listaCasos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
            String linea;
            
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");

                if (partes.length == 4) {
                    String idAsalto = partes[0];
                    String claveJuez = partes[1];
                    // Convierto los textos a boolean e int respectivamente, "parseo"
                    boolean condenado = Boolean.parseBoolean(partes[2]);
                    int mesesCarcel = Integer.parseInt(partes[3]);

                    CasoJudicial caso = new CasoJudicial(idAsalto, claveJuez, condenado, mesesCarcel);
                    listaCasos.add(caso);
                }
            }
        } catch (IOException e) {
            throw new ErrorAlLeerException("Archivo de Casos Judiciales", e.getMessage());
        }
        return listaCasos;
    }

    @Override
    public CasoJudicial buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException {
        List<CasoJudicial> casos = obtenerTodos();
        for (CasoJudicial caso : casos) {
            if (caso.getIdAsalto().equals(id)) {
                return caso; 
            }
        }
        throw new ObjetoNoEncontradoException("Caso Judicial", id);
    }

    @Override
    public void actualizar(CasoJudicial entidad) throws ErrorAlActualizarException {
        List<CasoJudicial> casos;
        //Intento Leer
        try {
            casos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException("Caso Judicial", "No se pudo leer el archivo original: " + e.getMessage());
        }
        //Intento Actualizar
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (CasoJudicial c : casos) {
                if (c.getIdAsalto().equals(entidad.getIdAsalto())) {
                    bw.write(entidad.getIdAsalto() + "," + 
                             entidad.getClaveJuez() + "," + 
                             entidad.isCondenado() + "," + 
                             entidad.getMesesCarcel());
                } else {
                    bw.write(c.getIdAsalto() + "," + 
                             c.getClaveJuez() + "," + 
                             c.isCondenado() + "," + 
                             c.getMesesCarcel());
                }
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ErrorAlActualizarException("Caso Judicial", "No se pudo escribir en el archivo: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(String id) throws ErrorAlEliminarException {
        List<CasoJudicial> casos;

        try {
            casos = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException("Caso Judicial", "No se pudo leer el archivo original: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (CasoJudicial c : casos) {
                if (!c.getIdAsalto().equals(id)) { 
                    bw.write(c.getIdAsalto() + "," + 
                             c.getClaveJuez() + "," + 
                             c.isCondenado() + "," + 
                             c.getMesesCarcel());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new ErrorAlEliminarException("Caso Judicial", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }
}