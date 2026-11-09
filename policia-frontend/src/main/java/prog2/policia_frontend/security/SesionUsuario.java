package prog2.policia_frontend.security;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.util.HashSet;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import prog2.policia_frontend.dtos.UsuarioDTO;

//Guarda y consulta permisos
@Component
@VaadinSessionScope
public class SesionUsuario {

    private UsuarioDTO usuario;
    private String contrasena;
    private Set<String> permisos = new HashSet<>();

    public void iniciarSesion(ResultadoAutenticacion resultado, String contrasena) {
        if (resultado == null || resultado.getUsuario() == null) {
            throw new IllegalArgumentException("El resultado de autenticación es obligatorio.");
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

    public void aplicarAutenticacion(HttpHeaders encabezados) {
        if (!estaAutenticado() || contrasena == null) {
            throw new IllegalStateException("No existe una sesión autenticada.");
        }
        encabezados.setBasicAuth(usuario.getCodigo(), contrasena);
    }

    public void cerrarSesion() {
        usuario = null;
        contrasena = null;
        permisos.clear();
    }

    public void actualizarContrasena(String nuevaContrasena) {
        if (!estaAutenticado()) {
            throw new IllegalStateException("No existe una sesión autenticada.");
        }
        if (nuevaContrasena == null || nuevaContrasena.isBlank()) {
            throw new IllegalArgumentException("La nueva contraseña es obligatoria.");
        }
        this.contrasena = nuevaContrasena;
    }

    public boolean tieneRol(String rol) {
        if (!estaAutenticado() || usuario.getRol() == null) {
            return false;
        }
        return usuario.getRol().equalsIgnoreCase(rol);
    }

    public boolean tienePermiso(String permisoBuscado) {
        if (!estaAutenticado() || permisoBuscado == null || permisoBuscado.isBlank()) {
            return false;
        }
        for (String permiso : permisos) {
            if (permiso != null && permiso.equalsIgnoreCase(permisoBuscado)) {
                return true;
            }
        }
        return false;
    }
}
