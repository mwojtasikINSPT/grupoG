
package prog2.policia_frontend.security;


import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.stereotype.Component;
import prog2.policia_frontend.dtos.UsuarioDTO;

//si entran con dif sesiones no se mezclan los datos
@Component
@VaadinSessionScope
public class SesionUsuario {

    private UsuarioDTO usuario;
    private String contrasena;
    
    
    /*
 * Guarda los datos cuando el backend
 * confirma que las credenciales son correctas.
 */
public void iniciarSesion(
        UsuarioDTO usuario,
        String contrasena) {

    this.usuario = usuario;
    this.contrasena = contrasena;
}

/*
 * Comprueba si actualmente existe
 * un usuario autenticado.
 */
public boolean estaAutenticado() {

    return usuario != null;
}


/*
 * Devuelve los datos del usuario conectado.
 * No devuelve la contraseña.
 */
public UsuarioDTO getUsuario() {

    return usuario;
}

    public void cerrarSesion() {

    // Borra de la sesión los datos del usuario autenticado.
    usuario = null;
    contrasena = null;

   
}
    /*
 * Comprueba si el usuario conectado
 * posee el rol recibido.
 */
    public boolean tieneRol(String rol) {

        if (!estaAutenticado()) {
            return false;
        }

        if (usuario.getRol() == null) {
            return false;
        }

        return usuario.getRol().equalsIgnoreCase(rol);
    }
}
