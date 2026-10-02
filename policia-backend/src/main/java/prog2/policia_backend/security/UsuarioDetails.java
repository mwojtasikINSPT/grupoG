package prog2.policia_backend.security;

import java.util.Collection;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import prog2.policia_backend.models.RolUsuario;

// Adapta nuestro usuario al modelo de Spring Security.
@Getter
@RequiredArgsConstructor
public class UsuarioDetails implements UserDetails {

    private final Long id;
    private final String codigo;
    private final String password;
    private final RolUsuario rol;
    private final boolean activo;

    // Rol que Spring utilizará para autorizar.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + rol.name())
        );
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