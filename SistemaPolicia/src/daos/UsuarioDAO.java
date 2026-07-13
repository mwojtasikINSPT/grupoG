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
import models.Rol;
import models.Usuario;
import models.UsuarioAdministrador;
import models.UsuarioInvestigador;
import models.UsuarioVigilante;
import models.Vigilante;

/**
 * Clase que implementa la persistencia de datos para la entidad {@link Usuario}
 * utilizando un archivo de texto plano.
 */
public class UsuarioDAO implements IGenericDAO<Usuario> {

    private final String RUTA_ARCHIVO = "usuarios.txt";

    /**
     * Constructor que inicializa el DAO y asegura la existencia del archivo de
     * datos.
     */
    public UsuarioDAO() {
        crearArchivoSiNoExiste();
    }

    /**
     * Verifica la existencia del archivo de texto. Si no existe, intenta
     * crearlo. En caso de error de E/S, imprime el mensaje de error por
     * consola.
     */
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

    /**
     * Verifica si existe un usuario con el nombre de usuario especificado.
     *
     * @param username El nombre de usuario a verificar.
     * @return {@code true} si se encuentra el usuario, {@code false} en caso
     *         contrario.
     */
    private boolean existeUsuario(String username) {
        try {
            buscarPorId(username);
            return true;
        } catch (ObjetoNoEncontradoException | ErrorAlLeerException e) {
            return false;
        }
    }

    /**
     * Genera una cadena de texto (formato CSV) representando al usuario. Si el
     * usuario es de tipo {@link UsuarioVigilante}, incluye información
     * adicional.
     *
     * * @param entidad El objeto usuario a convertir.
     * 
     * @return Una cadena formateada con los atributos del usuario separados por
     *         comas.
     */
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

    /**
     * Guarda un usuario en el archivo de texto. Valida previamente que el
     * nombre de usuario no esté duplicado.
     *
     * * @param entidad El usuario a persistir.
     * 
     * @throws ErrorAlGuardarException Si el usuario ya existe o hay problemas
     *                                 al escribir en el archivo.
     */
    @Override
    public void guardar(Usuario entidad) throws ErrorAlGuardarException {
        if (existeUsuario(entidad.getUsername())) {
            throw new ErrorAlGuardarException(
                    "Usuario",
                    "Ya existe un usuario con el nombre '" + entidad.getUsername() + "'.");
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            bw.write(armarLinea(entidad));
            bw.newLine();
        } catch (IOException e) {
            throw new ErrorAlGuardarException("Usuario", e.getMessage());
        }
    }

    /**
     * Recupera todos los usuarios almacenados en el archivo.
     *
     * * @return Una {@link List} con todos los objetos de tipo {@link Usuario}
     * reconstruidos.
     * 
     * @throws ErrorAlLeerException Si ocurre un error al acceder o procesar el
     *                              archivo.
     */
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

                // Si no tiene comas, el split devuelve solo 1 parte. Saltamos la línea
                // corrupta.
                if (partes.length < 3) {
                    System.out.println("ADVERTENCIA: Línea corrupta ignorada: " + linea);
                    continue;
                }
                // Verifico que haya AL MENOS 3 datos
                if (partes.length >= 3) {
                    String username = partes[0];
                    String password = partes[1];
                    String rolTexto = partes[2];

                    // Inicializo user
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

    /**
     * Busca un usuario específico mediante su nombre de usuario (username).
     *
     * * @param id El username del usuario a buscar.
     * 
     * @return El objeto {@link Usuario} encontrado.
     * @throws ObjetoNoEncontradoException Si no se encuentra el usuario.
     * @throws ErrorAlLeerException        Si hay problemas de acceso al archivo.
     */
    @Override
    public Usuario buscarPorId(String id) throws ObjetoNoEncontradoException, ErrorAlLeerException {
        List<Usuario> usuarios = obtenerTodos();
        for (Usuario usuario : usuarios) {
            if (usuario.getUsername().equalsIgnoreCase(id)) {
                return usuario;
            }
        }
        throw new ObjetoNoEncontradoException("Usuario", id);
    }

    /**
     * Actualiza la información de un usuario existente, sobrescribiendo el
     * archivo.
     *
     * * @param entidad El usuario con los nuevos datos.
     * 
     * @throws ErrorAlActualizarException Si hay un error al leer o sobrescribir
     *                                    el archivo.
     */
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

    /**
     * Elimina un usuario del archivo mediante su nombre de usuario.
     *
     * * @param id El username del usuario a eliminar.
     * 
     * @throws ErrorAlEliminarException Si hay errores al realizar la operación
     *                                  de escritura/lectura.
     */
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
