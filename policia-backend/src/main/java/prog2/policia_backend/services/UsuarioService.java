package prog2.policia_backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import prog2.policia_backend.DTOs.CambioPasswordDTO;
import prog2.policia_backend.DTOs.RestablecerPasswordDTO;
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
import prog2.policia_backend.DTOs.ResultadoBajaUsuarioDTO;
import prog2.policia_backend.security.SeguridadAdministrativa;
import org.springframework.security.access.AccessDeniedException;
import prog2.policia_backend.repositories.RolRepository;
import prog2.policia_backend.utils.GeneradorCodigo;
import prog2.policia_backend.utils.NormalizadorTexto;
import prog2.policia_backend.utils.ValidadorEdad;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final List<ReglaSalidaRol> reglasSalidaRol;
    private final SeguridadAdministrativa seguridadAdministrativa;

    public List<UsuarioDTO> listar(Boolean activo) {
        List<Usuario> usuarios;

        if (activo == null) {
            usuarios = usuarioRepository.findAll();
        } else {
            usuarios = usuarioRepository.findByActivo(activo);
        }

        List<UsuarioDTO> resultado = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            resultado.add(convertirADTO(usuario));
        }

        return resultado;
    }

    public List<UsuarioDTO> listarPropio(String codigo, Boolean activo) {
        Usuario usuario = obtenerUsuario(codigo);
        List<UsuarioDTO> resultado = new ArrayList<>();

        if (activo != null && usuario.isActivo() != activo) {
            return resultado;
        }

        resultado.add(convertirADTO(usuario));

        return resultado;
    }

    public UsuarioDTO buscarPorCodigo(String codigo) {
        return convertirADTO(obtenerUsuario(codigo));
    }

    @Transactional
    public UsuarioDTO guardar(UsuarioDTO dto) {
        String nombreRol = NormalizadorTexto.normalizarConstante(dto.getRol());
        Rol rol = obtenerRol(nombreRol);

        ValidadorEdad.validar(dto.getFechaNacimiento(), 18, 70);

        Usuario usuario = new Usuario();
        usuario.setNombre(NormalizadorTexto.normalizarParaGuardar(dto.getNombre()));
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(rol);
        usuario.setActivo(true);

        usuario = usuarioRepository.save(usuario);

        usuario.setCodigo(generarCodigoDisponible(rol.getPrefijo()));

        usuario = usuarioRepository.save(usuario);

        return convertirADTO(usuario);
    }

    @Transactional
    public UsuarioDTO actualizar(String codigo, UsuarioDTO dto) {
        Usuario usuario = obtenerUsuarioBloqueado(codigo);
        int desvinculados = 0;

        if (!usuario.isActivo()) {
            throw new PersonaInactivaException();
        }

        if (dto.getRol() != null) {
            String nombreRol = NormalizadorTexto.normalizarConstante(dto.getRol());

            if (!usuario.getRol().getNombre().equalsIgnoreCase(nombreRol)) {
                exigirAdministrador();
                Rol nuevoRol = obtenerRol(nombreRol);
                desvinculados = prepararSalidaRol(usuario);
                usuario.setRol(nuevoRol);
            }
        }

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            usuario.setNombre(NormalizadorTexto.normalizarParaGuardar(dto.getNombre()));
        }

        if (dto.getFechaNacimiento() != null) {
            ValidadorEdad.validar(dto.getFechaNacimiento(), 18, 70);
            usuario.setFechaNacimiento(dto.getFechaNacimiento());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        UsuarioDTO respuesta = convertirADTO(usuarioRepository.save(usuario));
        respuesta.setDefendidosDesvinculados(desvinculados);
        return respuesta;
    }

    @Transactional
    public ResultadoBajaUsuarioDTO eliminar(String codigo, UsuarioDTO dto) {
        Usuario usuario = obtenerUsuarioBloqueado(codigo);

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

        int desvinculados = prepararSalidaRol(usuario);
        usuario.setMotivoBaja(dto.getMotivoBaja());
        usuario.setActivo(false);

        usuarioRepository.save(usuario);
        return new ResultadoBajaUsuarioDTO(usuario.getCodigo(), desvinculados,
                "Usuario dado de baja. Defendidos desvinculados: " + desvinculados);
    }

    private int prepararSalidaRol(Usuario usuario) {
        int desvinculados = 0;
        for (ReglaSalidaRol regla : reglasSalidaRol) {
            desvinculados += regla.prepararSalida(usuario);
        }
        return desvinculados;
    }

    private void exigirAdministrador() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!seguridadAdministrativa.esAdministrador(authentication)) {
            throw new AccessDeniedException("Solo un administrador puede cambiar el rol");
        }
    }

    private Usuario obtenerUsuarioBloqueado(String codigo) {
        Optional<Usuario> resultado = usuarioRepository.findConBloqueoByCodigo(codigo.trim().toUpperCase(java.util.Locale.ROOT));
        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Usuario", codigo);
        }
        return resultado.get();
    }

    public UsuarioDTO reactivar(String codigo) {
        Usuario usuario = obtenerUsuario(codigo);

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
        dto.setFechaNacimiento(usuario.getFechaNacimiento());

        if (usuario.getFechaNacimiento() != null) {
            dto.setEdad(usuario.getEdad());
        }

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

        Rol rol = resultado.get();

        if (!Boolean.TRUE.equals(rol.getActivo())) {
            throw new OperacionNoPermitidaException("No se puede crear un usuario con un rol inactivo.");
        }

        return rol;
    }

    @Transactional
    public void cambiarPassword(CambioPasswordDTO dto) {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();

        Usuario usuario = obtenerUsuario(autenticacion.getName());

        if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPassword())) {
            throw new OperacionNoPermitidaException("La contraseña actual es incorrecta");
        }

        if (!usuario.isActivo()) {
            throw new PersonaInactivaException();
        }

        usuario.setPassword(passwordEncoder.encode(dto.getPasswordNueva()));

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void restablecerPassword(String codigo, RestablecerPasswordDTO dto) {
        Usuario usuario = obtenerUsuario(codigo);

        if (!usuario.isActivo()) {
            throw new PersonaInactivaException();
        }

        usuario.setPassword(passwordEncoder.encode(dto.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }
}
