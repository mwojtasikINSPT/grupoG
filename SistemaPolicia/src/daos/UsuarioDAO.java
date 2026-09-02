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

/**
 * Gestiona la persistencia de usuarios mediante un archivo de texto.
 *
 * Implementa {@link IGenericDAO} para permitir que esta forma de
 * almacenamiento pueda reemplazarse posteriormente por MySQL.
 *
 * @author GrupoG
 */
public class UsuarioDAO
        implements IGenericDAO<Usuario> {

    private static final String RUTA_ARCHIVO = "usuarios.txt";

    /**
     * Crea el DAO y comprueba que exista el archivo de usuarios.
     */
    public UsuarioDAO() {
        crearArchivoSiNoExiste();
    }

    /**
     * Crea el archivo de usuarios cuando todavía no existe.
     */
    private void crearArchivoSiNoExiste() {
        try {
            File archivo = new File(RUTA_ARCHIVO);

            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            System.out.println(
                    "Error al crear el archivo de usuarios: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Comprueba si existe un usuario con el nombre indicado.
     *
     * @param username nombre del usuario buscado
     * @return {@code true} si el usuario existe
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
     * Convierte un usuario en una línea de texto separada por comas.
     *
     * Para una cuenta de vigilante incorpora únicamente el código del
     * vigilante asociado.
     *
     * @param usuario usuario que se convertirá
     * @return línea preparada para almacenarse
     */
    private String armarLinea(Usuario usuario) {
        String linea = usuario.getUsername() + ","
                + usuario.getPassword() + ","
                + usuario.obtenerRol().name();

        if (usuario instanceof UsuarioVigilante usuarioVigilante) {
            linea += ","
                    + usuarioVigilante.getCodigoVigilante();
        }

        return linea;
    }

    /**
     * Guarda un usuario si su nombre todavía no está registrado.
     *
     * @param usuario usuario que se guardará
     * @throws ErrorAlGuardarException si el usuario ya existe o no puede
     * escribirse el archivo
     */
    @Override
    public void guardar(Usuario usuario)
            throws ErrorAlGuardarException {

        if (existeUsuario(usuario.getUsername())) {
            throw new ErrorAlGuardarException(
                    "Usuario",
                    "Ya existe un usuario con el nombre '"
                    + usuario.getUsername() + "'."
            );
        }

        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(RUTA_ARCHIVO, true))) {

            escritor.write(armarLinea(usuario));
            escritor.newLine();

        } catch (IOException e) {
            throw new ErrorAlGuardarException(
                    "Usuario",
                    e.getMessage()
            );
        }
    }

    /**
     * Recupera todos los usuarios almacenados en el archivo.
     *
     * Las líneas vacías o incompletas se ignoran para evitar que un registro
     * defectuoso interrumpa toda la lectura.
     *
     * @return lista de usuarios almacenados
     * @throws ErrorAlLeerException si no puede leerse el archivo
     */
    @Override
    public List<Usuario> obtenerTodos()
            throws ErrorAlLeerException {

        List<Usuario> usuarios = new ArrayList<>();

        try (BufferedReader lector = new BufferedReader(
                new FileReader(RUTA_ARCHIVO))) {

            String linea;

            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(",");

                if (partes.length < 3) {
                    System.out.println(
                            "ADVERTENCIA: Línea corrupta ignorada: "
                            + linea
                    );
                    continue;
                }

                String username = partes[0];
                String password = partes[1];
                Rol rol = Rol.valueOf(partes[2].toUpperCase());

                Usuario usuario = crearUsuario(
                        username,
                        password,
                        rol,
                        partes
                );

                if (usuario != null) {
                    usuarios.add(usuario);
                }
            }

        } catch (IOException e) {
            throw new ErrorAlLeerException(
                    "Archivo de Usuarios",
                    e.getMessage()
            );
        }

        return usuarios;
    }

    /**
     * Crea el tipo concreto de usuario correspondiente al rol leído.
     *
     * @param username nombre utilizado para iniciar sesión
     * @param password contraseña almacenada
     * @param rol rol asignado
     * @param partes columnas obtenidas del archivo
     * @return usuario construido a partir de los datos
     */
    private Usuario crearUsuario(
            String username,
            String password,
            Rol rol,
            String[] partes) {

        return switch (rol) {
            case ADMINISTRADOR ->
                new UsuarioAdministrador(username, password);

            case INVESTIGADOR ->
                new UsuarioInvestigador(username, password);

            case VIGILANTE -> {
                String codigoVigilante =
                        partes.length >= 4 ? partes[3] : "";

                yield new UsuarioVigilante(
                        username,
                        password,
                        codigoVigilante
                );
            }
        };
    }

    /**
     * Busca un usuario por su nombre de acceso.
     *
     * @param id nombre del usuario buscado
     * @return usuario encontrado
     * @throws ObjetoNoEncontradoException si el usuario no existe
     * @throws ErrorAlLeerException si no puede leerse el archivo
     */
    @Override
    public Usuario buscarPorId(String id)
            throws ObjetoNoEncontradoException, ErrorAlLeerException {

        List<Usuario> usuarios = obtenerTodos();

        for (Usuario usuario : usuarios) {
            if (usuario.getUsername().equalsIgnoreCase(id)) {
                return usuario;
            }
        }

        throw new ObjetoNoEncontradoException("Usuario", id);
    }

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param usuario usuario que contiene los nuevos datos
     * @throws ErrorAlActualizarException si el archivo no puede leerse o
     * reescribirse
     */
    @Override
    public void actualizar(Usuario usuario)
            throws ErrorAlActualizarException {

        List<Usuario> usuarios;

        try {
            usuarios = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlActualizarException(
                    "Usuario",
                    "No se pudo leer el archivo original: "
                    + e.getMessage()
            );
        }

        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(RUTA_ARCHIVO))) {

            for (Usuario usuarioGuardado : usuarios) {
                if (usuarioGuardado.getUsername()
                        .equals(usuario.getUsername())) {

                    escritor.write(armarLinea(usuario));
                } else {
                    escritor.write(armarLinea(usuarioGuardado));
                }

                escritor.newLine();
            }

        } catch (IOException e) {
            throw new ErrorAlActualizarException(
                    "Usuario",
                    "No se pudo escribir en el archivo: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Elimina el usuario que tenga el nombre indicado.
     *
     * @param id nombre del usuario que se eliminará
     * @throws ErrorAlEliminarException si el archivo no puede leerse o
     * reescribirse
     */
    @Override
    public void eliminar(String id)
            throws ErrorAlEliminarException {

        List<Usuario> usuarios;

        try {
            usuarios = obtenerTodos();
        } catch (ErrorAlLeerException e) {
            throw new ErrorAlEliminarException(
                    "Usuario",
                    "No se pudo leer el archivo original: "
                    + e.getMessage()
            );
        }

        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(RUTA_ARCHIVO))) {

            for (Usuario usuario : usuarios) {
                if (!usuario.getUsername().equalsIgnoreCase(id)) {
                    escritor.write(armarLinea(usuario));
                    escritor.newLine();
                }
            }

        } catch (IOException e) {
            throw new ErrorAlEliminarException(
                    "Usuario",
                    "No se pudo eliminar el registro: "
                    + e.getMessage()
            );
        }
    }
}