package prog2.policia_backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import prog2.policia_backend.repositories.UsuarioRepository;
import prog2.policia_backend.DTOs.UsuarioDTO;
import prog2.policia_backend.exceptions.MotivoBajaObligatorioException;
import prog2.policia_backend.exceptions.OperacionNoPermitidaException;
import prog2.policia_backend.exceptions.PersonaInactivaException;
import prog2.policia_backend.exceptions.PersonaNoReactivableException;
import prog2.policia_backend.exceptions.PersonaYaActivaException;
import prog2.policia_backend.exceptions.PersonaYaInactivaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.MotivoBajaPersona;
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
        List<Usuario> usuarios;

        if (activo == null) {
            usuarios = usuarioRepository.findAll();
        } else {
            usuarios = usuarioRepository.findByActivo(activo);
        }

        List<UsuarioDTO> resultado = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            if (usuario instanceof UsuarioGenerico) {
                resultado.add(convertirADTO(usuario));
            }
        }

        return resultado;
    }

    public List<UsuarioDTO> listarPropio(String codigo, Boolean activo, String nombre) {
        Usuario usuario = obtenerUsuario(codigo);
        List<UsuarioDTO> resultado = new ArrayList<>();

        if (!(usuario instanceof UsuarioGenerico)) {
            return resultado;
        }

        if (activo != null && usuario.isActivo() != activo) {
            return resultado;
        }

        if (nombre != null && !nombre.isBlank()) {
            String nombreNormalizado = NormalizadorTexto.normalizarParaBuscar(nombre);
            String nombreUsuario = NormalizadorTexto.normalizarParaBuscar(usuario.getNombre());

            if (!nombreUsuario.contains(nombreNormalizado)) {
                return resultado;
            }
        }

        resultado.add(convertirADTO(usuario));

        return resultado;
    }

    public List<UsuarioDTO> buscarPorNombre(String nombre, Boolean activo) {
        String nombreNormalizado = NormalizadorTexto.normalizarParaBuscar(nombre);
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<UsuarioDTO> resultado = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            if (!(usuario instanceof UsuarioGenerico)) {
                continue;
            }

            if (activo != null && usuario.isActivo() != activo) {
                continue;
            }

            String nombreUsuario = NormalizadorTexto.normalizarParaBuscar(usuario.getNombre());

            if (nombreUsuario.contains(nombreNormalizado)) {
                resultado.add(convertirADTO(usuario));
            }
        }

        return resultado;
    }

    public UsuarioDTO buscarPorCodigo(String codigo) {
        Usuario usuario = obtenerUsuario(codigo);

        if (!(usuario instanceof UsuarioGenerico)) {
            throw new RecursoNoEncontradoException("Usuario", codigo);
        }

        return convertirADTO(usuario);
    }

    public UsuarioDTO guardar(UsuarioDTO dto) {
        String nombreRol = NormalizadorTexto.normalizarConstante(dto.getRol());

        if (nombreRol.equals("VIGILANTE")) {
            throw new OperacionNoPermitidaException("Los vigilantes deben registrarse mediante VigilanteService");
        }

        Rol rol = obtenerRol(nombreRol);

        UsuarioGenerico usuario = new UsuarioGenerico();

        usuario.setNombre(NormalizadorTexto.normalizarParaGuardar(dto.getNombre()));
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(rol);
        usuario.setActivo(true);

        usuario = usuarioRepository.save(usuario);

        usuario.setCodigo(generarCodigoDisponible(rol.getPrefijo()));

        usuario = usuarioRepository.save(usuario);

        return convertirADTO(usuario);
    }

    public UsuarioDTO actualizar(String codigo, UsuarioDTO dto) {
        Usuario usuario = obtenerUsuario(codigo);

        if (!(usuario instanceof UsuarioGenerico)) {
            throw new RecursoNoEncontradoException("Usuario", codigo);
        }

        if (!usuario.isActivo()) {
            throw new PersonaInactivaException();
        }

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            usuario.setNombre(NormalizadorTexto.normalizarParaGuardar(dto.getNombre()));
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getRol() != null) {
            String nombreRol = NormalizadorTexto.normalizarConstante(dto.getRol());

            if (nombreRol.equals("VIGILANTE")) {
                throw new OperacionNoPermitidaException("No se puede asignar el rol VIGILANTE desde UsuarioService");
            }

            Rol rol = obtenerRol(nombreRol);
            usuario.setRol(rol);
            usuario.setCodigo(generarCodigoDisponible(rol.getPrefijo()));
        }

        return convertirADTO(usuarioRepository.save(usuario));
    }

    public void eliminar(String codigo, UsuarioDTO dto) {
        Usuario usuario = obtenerUsuario(codigo);

        if (!(usuario instanceof UsuarioGenerico)) {
            throw new RecursoNoEncontradoException("Usuario", codigo);
        }

        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();

        if (usuario.getRol().getNombre().equalsIgnoreCase("ADMINISTRADOR") && usuario.getCodigo().equals(autenticacion.getName())) {
            throw new OperacionNoPermitidaException("Un administrador no puede darse de baja a sí mismo");
        }

        if (!usuario.isActivo()) {
            throw new PersonaYaInactivaException();
        }

        if (dto.getMotivoBaja() == null) {
            throw new MotivoBajaObligatorioException();
        }

        usuario.setMotivoBaja(dto.getMotivoBaja());
        usuario.setActivo(false);

        usuarioRepository.save(usuario);
    }

    public UsuarioDTO reactivar(String codigo) {
        Usuario usuario = obtenerUsuario(codigo);

        if (!(usuario instanceof UsuarioGenerico)) {
            throw new RecursoNoEncontradoException("Usuario", codigo);
        }

        if (usuario.isActivo()) {
            throw new PersonaYaActivaException();
        }

        if (usuario.getMotivoBaja() == MotivoBajaPersona.FALLECIMIENTO) {
            throw new PersonaNoReactivableException();
        }

        usuario.setActivo(true);
        usuario.setMotivoBaja(null);

        return convertirADTO(usuarioRepository.save(usuario));
    }

    // Métodos de mapeo
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

    // Métodos auxiliares
    private Usuario obtenerUsuario(String codigo) {
        String codigoNormalizado = codigo != null ? codigo.trim().toUpperCase() : null;

        Optional<Usuario> resultado = usuarioRepository.findByCodigo(codigoNormalizado);

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Usuario", "código: " + codigo);
        }

        return resultado.get();
    }

    private String generarCodigoDisponible(String prefijo) {
        long numero = 1;
        String codigo;

        do {
            codigo = GeneradorCodigo.generar(prefijo, numero);
            numero++;
        } while (usuarioRepository.existsByCodigo(codigo));

        return codigo;
    }

    private Rol obtenerRol(String nombreRol) {
        Optional<Rol> resultado = rolRepository.findByNombre(nombreRol);

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Rol", nombreRol);
        }

        return resultado.get();
    }

}
