package prog2.policia_backend.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import prog2.policia_backend.repositories.UsuarioRepository;
import prog2.policia_backend.DTOs.UsuarioDTO;
import prog2.policia_backend.exceptions.MotivoBajaObligatorioException;
import prog2.policia_backend.exceptions.PersonaYaActivaException;
import prog2.policia_backend.exceptions.PersonaYaInactivaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Rol;
import prog2.policia_backend.models.Usuario;
import prog2.policia_backend.models.UsuarioGenerico;
import prog2.policia_backend.repositories.RolRepository;
import prog2.policia_backend.utils.GeneradorCodigo;
import prog2.policia_backend.utils.NormalizadorTexto;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioDTO> listar(Boolean activo) {

        List<Usuario> usuarios = activo == null
                ? usuarioRepository.findAll()
                : usuarioRepository.findByActivo(activo);

        return usuarios.stream()
                .filter(u -> u instanceof UsuarioGenerico)
                .map(this::convertirADTO)
                .toList();
    }

    public List<UsuarioDTO> buscarPorNombre(String nombre, Boolean activo) {

        return usuarioRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .filter(u -> u instanceof UsuarioGenerico)
                .filter(u -> activo == null || u.isActivo() == activo)
                .map(this::convertirADTO)
                .toList();
    }

    public UsuarioDTO buscarPorCodigo(String codigo) {

        Usuario usuario = obtenerUsuario(codigo);

        if (!(usuario instanceof UsuarioGenerico)) {
            throw new RecursoNoEncontradoException("Usuario", codigo);
        }

        return convertirADTO(usuario);
    }

    public UsuarioDTO guardar(UsuarioDTO dto) {

        Rol rol = rolRepository.findByNombre(NormalizadorTexto.normalizarConstante(dto.getRol())).orElseThrow(()
                -> new RecursoNoEncontradoException("Rol", dto.getRol()));

        UsuarioGenerico usuario = new UsuarioGenerico();

        usuario.setNombre(dto.getNombre());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(rol);
        usuario.setActivo(true);

        usuario = usuarioRepository.save(usuario);

        usuario.setCodigo(GeneradorCodigo.generar(rol.getPrefijo(), usuario.getId()));

        usuario = usuarioRepository.save(usuario);

        return convertirADTO(usuario);
    }

    public UsuarioDTO actualizar(String codigo, UsuarioDTO dto) {

        Usuario usuario = obtenerUsuario(codigo);

        if (!(usuario instanceof UsuarioGenerico)) {
            throw new RecursoNoEncontradoException("Usuario", codigo);
        }

        if (dto.getNombre() != null) {
            usuario.setNombre(dto.getNombre());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getRol() != null) {
            Rol rol = rolRepository.findByNombre(NormalizadorTexto.normalizarConstante(dto.getRol()))
                    .orElseThrow(() -> new RecursoNoEncontradoException("Rol", dto.getRol()));

            usuario.setRol(rol);
        }

        return convertirADTO(usuarioRepository.save(usuario));
    }

    public UsuarioDTO eliminar(String codigo, UsuarioDTO dto) {

        Usuario usuario = obtenerUsuario(codigo);

        if (!(usuario instanceof UsuarioGenerico)) {
            throw new RecursoNoEncontradoException("Usuario", codigo);
        }

        if (!usuario.isActivo()) {
            throw new PersonaYaInactivaException();
        }

        if (dto.getMotivoBaja() == null) {
            throw new MotivoBajaObligatorioException();
        }

        usuario.setMotivoBaja(dto.getMotivoBaja());
        usuario.setActivo(false);

        return convertirADTO(usuarioRepository.save(usuario));
    }

    public UsuarioDTO reactivar(String codigo) {

        Usuario usuario = obtenerUsuario(codigo);

        if (!(usuario instanceof UsuarioGenerico)) {
            throw new RecursoNoEncontradoException("Usuario", codigo);
        }

        if (usuario.isActivo()) {
            throw new PersonaYaActivaException();
        }

        usuario.setActivo(true);
        usuario.setMotivoBaja(null);

        return convertirADTO(usuarioRepository.save(usuario));
    }

    //métodos de Mapeo
    private UsuarioDTO convertirADTO(Usuario usuario) {

        UsuarioDTO dto = new UsuarioDTO();

        dto.setMotivoBaja(usuario.getMotivoBaja());

        dto.setId(usuario.getId());
        dto.setCodigo(usuario.getCodigo());
        dto.setNombre(usuario.getNombre());
        dto.setRol(usuario.getRol().getNombre());
        dto.setActivo(usuario.isActivo());

        dto.setFechaCreacion(usuario.getFechaCreacion());
        dto.setFechaModificacion(usuario.getFechaModificacion());
        dto.setCreadoPor(usuario.getCreadoPor());
        dto.setModificadoPor(usuario.getModificadoPor());

        return dto;
    }

    private Usuario obtenerUsuario(String codigo) {

        String codigoNormalizado = codigo != null ? codigo.trim().toUpperCase() : null;

        return usuarioRepository.findByCodigo(codigoNormalizado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", "código: " + codigo));
    }
}
