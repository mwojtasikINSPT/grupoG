package prog2.policia_backend.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import prog2.policia_backend.models.RolPermisoEntidad;

@Getter
@RequiredArgsConstructor
public class UsuarioDetails implements UserDetails {

    private final Long id;
    private final String codigo;
    private final String password;
    private final String nombreRol;
    private final Set<RolPermisoEntidad> permisosPorEntidad;
    private final boolean activo;

    // Convierte los permisos del rol en autoridades para Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        for (RolPermisoEntidad relacion : permisosPorEntidad) {
            String autoridad = relacion.getPermiso().name() + "_" + relacion.getEntidad().name();
            authorities.add(new SimpleGrantedAuthority(autoridad));
        }

        if ("ADMINISTRADOR".equals(nombreRol)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));
        }

        return authorities;
    }

    // Contraseña almacenada
    @Override
    public String getPassword() {
        return password;
    }

    // Spring usa el código como nombre de usuario
    @Override
    public String getUsername() {
        return codigo;
    }

    // La cuenta no vence.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // La cuenta no está bloqueada.
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Las credenciales no vencen.
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Solo usuarios activos pueden autenticarse.
    @Override
    public boolean isEnabled() {
        return activo;
    }
}