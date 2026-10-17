
package prog2.policia_frontend.security;


import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.stereotype.Component;
import prog2.policia_frontend.dtos.UsuarioDTO;
import org.springframework.http.HttpHeaders;

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

/*
 * Coloca las credenciales del usuario conectado
 * en una petición dirigida al backend.
 *
 * De esta manera, las vistas y los clientes
 * no necesitan obtener directamente la contraseña.
 */
public void aplicarAutenticacion(
        HttpHeaders encabezados) {

    if (!estaAutenticado()
            || contrasena == null) {

        throw new IllegalStateException(
                "No existe una sesión autenticada."
        );
    }

    encabezados.setBasicAuth(
            usuario.getCodigo(),
            contrasena
    );
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
    /*
 * Comprueba si el usuario conectado
 * posee un permiso determinado.
 */ 
    public boolean tienePermiso(String permisoBuscado) {

  
    if (!estaAutenticado()) {
        return false;
    }

  
    if (permisoBuscado == null
            || permisoBuscado.isBlank()) {
        return false;
    }

    // Evita errores si el backend todavía
    // no envía la lista de permisos.
    if (usuario.getPermisos() == null) {
        return false;
    }

    /*
     * Recorre los permisos del usuario y comprueba
     * si alguno coincide con el permiso buscado.
     */
    return usuario.getPermisos()
            .stream()
            .anyMatch(permiso
                    -> permiso != null
                    && permiso.equalsIgnoreCase(
                            permisoBuscado
                    )
            );
}
}
