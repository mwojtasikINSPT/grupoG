package prog2.policia_backend.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.RolDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.exceptions.RolExistenteException;
import prog2.policia_backend.exceptions.RolYaActivoException;
import prog2.policia_backend.exceptions.RolYaInactivoException;
import prog2.policia_backend.models.Rol;
import prog2.policia_backend.repositories.RolRepository;
import prog2.policia_backend.utils.GeneradorCodigo;
import prog2.policia_backend.utils.NormalizadorTexto;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    public List<RolDTO> listar(Boolean activo) {
        List<Rol> roles;

        if (activo == null) {
            roles = rolRepository.findAll();
        } else {
            roles = rolRepository.findByActivo(activo);
        }

        return roles.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public RolDTO buscarPorCodigo(String codigo) {
        return convertirADTO(obtenerRol(codigo));
    }

    public RolDTO guardar(RolDTO dto) {

        String nombreNormalizado = NormalizadorTexto.normalizarConstante(dto.getNombre());

        if (rolRepository.existsByNombre(nombreNormalizado)) {
            throw new RolExistenteException();
        }

        Rol rol = convertirAEntidad(dto);
        rol.setActivo(true);

        if (rol.getCodigo() == null || rol.getCodigo().isBlank()) {
            rol.setCodigo("ROL_TEMP_" + System.currentTimeMillis());
        }

        rol = rolRepository.save(rol);
        rol.setCodigo(GeneradorCodigo.generar("ROL", rol.getId()));
        rol = rolRepository.save(rol);

        return convertirADTO(rol);
    }

    public RolDTO actualizar(String codigo, RolDTO dto) {
        Rol rol = obtenerRol(codigo);

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            String nombreNormalizado = NormalizadorTexto.normalizarConstante(dto.getNombre());

            if (!rol.getNombre().equals(nombreNormalizado)) {
                if (rolRepository.existsByNombre(nombreNormalizado)) {
                    throw new RolExistenteException();
                }
                rol.setNombre(nombreNormalizado);
            }
        }
        if (dto.getDescripcion() != null) {
            rol.setDescripcion(NormalizadorTexto.normalizarParaGuardar(dto.getDescripcion()));
        }
        if (dto.getPrefijo() != null) {
            rol.setPrefijo(NormalizadorTexto.normalizarConstante(dto.getPrefijo()));
        }
        if (dto.getPermisos() != null) {
            rol.setPermisos(dto.getPermisos());
        }

        return convertirADTO(rolRepository.save(rol));
    }

    public RolDTO eliminar(String codigo) {
        Rol rol = obtenerRol(codigo);

        if (!rol.getActivo()) {
            throw new RolYaInactivoException();
        }

        rol.setActivo(false);
        return convertirADTO(rolRepository.save(rol));
    }

    public RolDTO reactivar(String codigo) {
        Rol rol = obtenerRol(codigo);

        if (rol.getActivo()) {
            throw new RolYaActivoException();
        }

        rol.setActivo(true);

        return convertirADTO(rolRepository.save(rol));
    }

    // --- Métodos de Mapeo ---
    private RolDTO convertirADTO(Rol rol) {
        RolDTO dto = new RolDTO();
        dto.setId(rol.getId());
        dto.setCodigo(rol.getCodigo());
        dto.setNombre(rol.getNombre());
        dto.setDescripcion(rol.getDescripcion());
        dto.setPrefijo(rol.getPrefijo());
        dto.setPermisos(rol.getPermisos());
        dto.setActivo(rol.getActivo());
        dto.setFechaCreacion(rol.getFechaCreacion());
        dto.setFechaModificacion(rol.getFechaModificacion());
        dto.setCreadoPor(rol.getCreadoPor());
        dto.setModificadoPor(rol.getModificadoPor());

        return dto;
    }

    private Rol convertirAEntidad(RolDTO dto) {
        Rol rol = new Rol();

        rol.setPrefijo(NormalizadorTexto.normalizarConstante(dto.getPrefijo()));
        rol.setCodigo(NormalizadorTexto.normalizarConstante(dto.getCodigo()));
        rol.setNombre(NormalizadorTexto.normalizarConstante(dto.getNombre()));
        rol.setDescripcion(NormalizadorTexto.normalizarParaGuardar(dto.getDescripcion()));
        rol.setPermisos(dto.getPermisos());

        return rol;
    }

    // --- Métodos Auxiliares ---
    private Rol obtenerRol(String codigo) {

        String codigoNormalizado = codigo != null ? codigo.trim().toUpperCase() : null;

        return rolRepository.findByCodigo(codigoNormalizado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol", "código: " + codigo));
    }
}
