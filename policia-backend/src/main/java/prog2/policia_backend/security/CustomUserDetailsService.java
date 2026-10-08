package prog2.policia_backend.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import prog2.policia_backend.models.Usuario;
import prog2.policia_backend.repositories.UsuarioRepository;

// Busca nuestros usuarios para que Spring Security pueda autenticarlos
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    // Spring llama a este método durante el login.
    @Override
    public UserDetails loadUserByUsername(String codigo)
            throws UsernameNotFoundException {

        // Buscamos directamente en la tabla unificada de usuarios por su código
        Usuario usuario = usuarioRepository.findByCodigo(codigo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return new UsuarioDetails(
                usuario.getId(),
                usuario.getCodigo(),
                usuario.getPassword(),
                usuario.getRol().getPermisos(),
                usuario.isActivo());
    }
}
