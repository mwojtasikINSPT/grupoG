package controllers;

//Gestiona usuariodao
import daos.UsuarioDAO;
import exceptions.ErrorAlEliminarException;
import exceptions.ErrorAlGuardarException;
import exceptions.ErrorAlLeerException;
import exceptions.ObjetoNoEncontradoException;
import java.util.List;
import models.Rol;
import models.Usuario;
import models.UsuarioAdministrador;
import models.UsuarioInvestigador;
import models.UsuarioVigilante;

public class UsuariosController {

    private final UsuarioDAO usuarioDAO;

    public UsuariosController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    //Registra un nuevo usuario en el sistema (.txt)  respetando su rol
    
    public void registrarUsuario(String nombreUsuario, String password, String rolEmp, String codigoVigilante) throws Exception {
        //Validamos 
        if (nombreUsuario.trim().isEmpty() || password.trim().isEmpty()) {
            throw new Exception("El nombre de usuario y la contraseña no pueden estar vacíos.");
        }
        try {
            try {
                //Validamos que no sea duplicado (nombre de usuario unico)
                usuarioDAO.buscarPorId(nombreUsuario);
                throw new Exception("El nombre de usuario '" + nombreUsuario + "' ya está en uso.");
            } catch (ObjetoNoEncontradoException e) {
                //si no lo encuentra, se puede usar
            }

            //Creamos el usuario ya verificado
            Usuario nuevoUsuario = null;
            Rol rol = Rol.valueOf(rolEmp.toUpperCase());
            switch (rol) {
                case ADMINISTRADOR:
                    nuevoUsuario = new UsuarioAdministrador(nombreUsuario, password);
                    break;
                case INVESTIGADOR:
                    nuevoUsuario = new UsuarioInvestigador(nombreUsuario, password);
                    break;
                case VIGILANTE:
                    if (codigoVigilante == null || codigoVigilante.trim().isEmpty()) {
                        throw new Exception("El rol VIGILANTE requiere especificar su código de vigilante.");
                     }
                    nuevoUsuario = new UsuarioVigilante(nombreUsuario, password, codigoVigilante);
                    break;
            }
            usuarioDAO.guardar(nuevoUsuario);
            }catch(IllegalArgumentException e) {
                throw new Exception("El rol '" + rolEmp + "' no es válido en el sistema.");
        }catch (ErrorAlGuardarException e) {
            throw new Exception("No se pudo guardar el usuario: " + e.getMessage());
        }
    }

    // listar todos los usuarios
    public List<Usuario> listarUsuarios() throws Exception{
        try{
            return usuarioDAO.obtenerTodos(); 
        }catch(ErrorAlLeerException e){
            throw new Exception("Error al recuperar la lista de usuarios: " + e.getMessage());
        }
    }
    
    // eliminar usuario mediante su nombre de usuario (es unico)
    
    public void eliminarUsuario(String nombreUsuario) throws Exception{
        if (nombreUsuario.equals("admin")){
            throw new Exception("Por motivos de seguridad, no se puede eliminar el usuario del admin.");
        }
        
        try{
            usuarioDAO.buscarPorId(nombreUsuario);
            usuarioDAO.eliminar(nombreUsuario);
        }catch (ObjetoNoEncontradoException e){
            throw new Exception ("El usuario '" + nombreUsuario + "' no existe en el sistema.");
        }catch (ErrorAlEliminarException e){
            throw new Exception("No se pudo eliminar el usuario: " + e.getMessage());
        }
    }
}
