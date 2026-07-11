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
import models.Rol;
import models.Usuario;
import models.UsuarioAdministrador;
import models.UsuarioInvestigador;
import models.UsuarioVigilante;
import models.Vigilante;

public class UsuarioDAO implements IGenericDAO<Usuario> {

    private final String RUTA_ARCHIVO = "usuarios.txt";

    public UsuarioDAO() {
        crearArchivoSiNoExiste();
    }

    private void crearArchivoSiNoExiste() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error al crear el archivo de usuarios: " + e.getMessage());
        }
    }

    // Método auxiliar para armar la línea de texto dependiendo del tipo de usuario
    private String armarLinea(Usuario entidad) {
        String linea = entidad.getUsername() + ","
                + entidad.getPassword() + ","
                + entidad.obtenerRol().name();

        // Si el usuario es un Vigilante, agrego la 4ta columna con su código
        if (entidad instanceof UsuarioVigilante) {
            // Transformo (casteo) la variable a UsuarioVigilante para poder usar su getter
            UsuarioVigilante vigilante = (UsuarioVigilante) entidad;
            linea += "," + vigilante.getVigilante().getCodigo();
        }
        return linea;
    }

    @Override
    public void guardar(Usuario entidad) throws ErrorAlGuardarException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            bw.write(armarLinea(entidad));
            bw.newLine();
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Usuario", e.getMessage());
        }
    }

    @Override
    public List<Usuario> obtenerTodos() throws ErrorAlLeerException {
        List<Usuario> listaUsuarios = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
            String linea;

            while ((linea = br.readLine()) != null) {

                if (linea.trim().isEmpty()) {
                    continue; // Ignora líneas vacías
                }
                String[] partes = linea.split(",");

                // Si no tiene comas, el split devuelve solo 1 parte. Saltamos la línea corrupta.
                if (partes.length < 3) {
                    System.out.println("ADVERTENCIA: Línea corrupta ignorada: " + linea);
                    continue;
                }
                // Verifico que haya AL MENOS 3 datos
                if (partes.length >= 3) {
                    String username = partes[0];
                    String password = partes[1];
                    String rolTexto = partes[2];

                    //Inicializo user
                    Usuario usuario = null;

                    // Convierto el texto del archivo al Enum real
                    Rol rol = Rol.valueOf(rolTexto.toUpperCase());

                    switch (rol) {
                        case ADMINISTRADOR:
                            usuario = new UsuarioAdministrador(username, password);
                            break;
                        case INVESTIGADOR:
                            usuario = new UsuarioInvestigador(username, password);
                            break;
                        case VIGILANTE:
                            // Si es vigilante, busco la 4ta columna (índice 3)
                            String codigoVigilante = "";
                            if (partes.length == 4) {
                                codigoVigilante = partes[3];
                            }
                            usuario = new UsuarioVigilante(username, password, new Vigilante(codigoVigilante, 0));
                            break;
                    }

                    if (usuario != null) {
                        listaUsuarios.add(usuario);
                    }
                }
            }
        } catch (IOException e) {
            throw new ErrorAlLeerException("Archivo de Usuarios", e.getMessage());
        }
        return listaUsuarios;
    }

    @Override
    public Usuario buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException {
        List<Usuario> usuarios = obtenerTodos();
        for (Usuario usuario : usuarios) {
            if (usuario.getUsername().equals(id)) {
                return usuario;
            }
        }
        throw new ObjetoNoEncontradoException("Usuario", id);
    }

    @Override
    public void actualizar(Usuario entidad) throws ErrorAlActualizarException {
        List<Usuario> usuarios;

        try {
            usuarios = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException("Usuario", "No se pudo leer el archivo original: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Usuario u : usuarios) {
                if (u.getUsername().equals(entidad.getUsername())) {
                    bw.write(armarLinea(entidad));
                } else {
                    bw.write(armarLinea(u));
                }
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ErrorAlActualizarException("Usuario", "No se pudo escribir en el archivo: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(String id) throws ErrorAlEliminarException {
        List<Usuario> usuarios;

        try {
            usuarios = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException("Usuario", "No se pudo leer el archivo original: " + e.getMessage());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Usuario u : usuarios) {
                if (!u.getUsername().equals(id)) {
                    bw.write(armarLinea(u));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new ErrorAlEliminarException("Usuario", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }
}
