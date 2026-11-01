
package prog2.policia_frontend.security;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;
import prog2.policia_frontend.dtos.UsuarioDTO;
import org.springframework.http.HttpHeaders;

/**
 * Conserva la identidad y los permisos de una única sesión de Vaadin.
 */
@Component
@VaadinSessionScope
public class SesionUsuario {

    private UsuarioDTO usuario;
    private String contrasena;
    private Set<String> permisos = new HashSet<>();

    public void iniciarSesion(
            ResultadoAutenticacion resultado,
            String contrasena) {

        if (resultado == null || resultado.getUsuario() == null) {
            throw new IllegalArgumentException(
                    "El resultado de autenticación es obligatorio."
            );
        }

        this.usuario = resultado.getUsuario();
        this.contrasena = contrasena;
        this.permisos = new HashSet<>(resultado.getPermisos());
    }

    public boolean estaAutenticado() {
        return usuario != null;
    }

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

        if (!estaAutenticado() || contrasena == null) {

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
        usuario = null;
        contrasena = null;
        permisos.clear();
    }

    public boolean tieneRol(String rol) {

        if (!estaAutenticado()) {
            return false;
        }

        if (usuario.getRol() == null) {
            return false;
        }

        return usuario.getRol().equalsIgnoreCase(rol);
    }
    public boolean tienePermiso(String permisoBuscado) {

        if (!estaAutenticado()
                || permisoBuscado == null
                || permisoBuscado.isBlank()) {
            return false;
        }

        return permisos.stream()
                .anyMatch(permiso
                        -> permiso != null
                        && permiso.equalsIgnoreCase(permisoBuscado));
    }
}
