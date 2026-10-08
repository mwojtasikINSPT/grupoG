package prog2.policia_backend.security;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import prog2.policia_backend.models.Permiso;

// Adapta nuestro usuario al modelo de Spring Security.
@Getter
@RequiredArgsConstructor
public class UsuarioDetails implements UserDetails {

    private final Long id;
    private final String codigo;
    private final String password;
    private final Set<Permiso> permisos;
    private final boolean activo;

    // Conierto  los permisos del rol en GrantedAuthority para Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permisos.stream()
                .map(permiso -> new SimpleGrantedAuthority(permiso.name()))
                .collect(Collectors.toList());
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
