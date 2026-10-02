package prog2.policia_backend.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import prog2.policia_backend.models.Administrador;
import prog2.policia_backend.models.Investigador;
import prog2.policia_backend.models.Vigilante;
import prog2.policia_backend.repositories.AdministradorRepository;
import prog2.policia_backend.repositories.InvestigadorRepository;
import prog2.policia_backend.repositories.VigilanteRepository;

// Busca nuestros usuarios para que Spring Security pueda autenticarlos.
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final VigilanteRepository vigilanteRepository;
    private final InvestigadorRepository investigadorRepository;
    private final AdministradorRepository administradorRepository;

    // Spring llama a este método durante el login.
    @Override
    public UserDetails loadUserByUsername(String codigo)
            throws UsernameNotFoundException {

        // Busca primero entre los vigilantes.
        if (codigo.startsWith("VIG")) {
            Vigilante vigilante = vigilanteRepository.findByCodigo(codigo)
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "Usuario no encontrado"));

            return new UsuarioDetails(
                    vigilante.getId(),
                    vigilante.getCodigo(),
                    vigilante.getPassword(),
                    vigilante.getRol(),
                    vigilante.isActivo()
            );
        }

        // Busca entre los investigadores.
        if (codigo.startsWith("INV")) {
            Investigador investigador = investigadorRepository.findByCodigo(codigo)
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "Usuario no encontrado"));

            return new UsuarioDetails(
                    investigador.getId(),
                    investigador.getCodigo(),
                    investigador.getPassword(),
                    investigador.getRol(),
                    investigador.isActivo()
            );
        }

        // Busca entre los administradores.
        if (codigo.startsWith("ADM")) {
            Administrador administrador =
                    administradorRepository.findByCodigo(codigo)
                            .orElseThrow(() ->
                                    new UsernameNotFoundException(
                                            "Usuario no encontrado"));

            return new UsuarioDetails(
                    administrador.getId(),
                    administrador.getCodigo(),
                    administrador.getPassword(),
                    administrador.getRol(),
                    administrador.isActivo()
            );
        }

        throw new UsernameNotFoundException(
                "Código de usuario inválido");
    }
}