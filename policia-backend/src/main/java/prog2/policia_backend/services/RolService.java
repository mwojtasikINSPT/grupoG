package prog2.policia_backend.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import prog2.policia_backend.DTOs.RolDTO;
import prog2.policia_backend.DTOs.RolPermisoEntidadDTO;
import prog2.policia_backend.exceptions.OperacionNoPermitidaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.exceptions.RolExistenteException;
import prog2.policia_backend.exceptions.RolYaActivoException;
import prog2.policia_backend.exceptions.RolYaInactivoException;
import prog2.policia_backend.models.Rol;
import prog2.policia_backend.models.RolPermisoEntidad;
import prog2.policia_backend.repositories.RolRepository;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;
import prog2.policia_backend.repositories.UsuarioRepository;
import prog2.policia_backend.utils.GeneradorCodigo;
import prog2.policia_backend.utils.NormalizadorTexto;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsaltanteRepository asaltanteRepository;
    private final ContratoVigilanciaRepository contratoVigilanciaRepository;

    public List<RolDTO> listar(Boolean activo) {
        List<Rol> roles;

        if (activo == null) {
            roles = rolRepository.findAll();
        } else {
            roles = rolRepository.findByActivo(activo);
        }

        List<RolDTO> resultado = new ArrayList<>();

        for (Rol rol : roles) {
            resultado.add(convertirADTO(rol));
        }

        return resultado;
    }

    public RolDTO buscarPorCodigo(String codigo) {
        return convertirADTO(obtenerRol(codigo));
    }

    @Transactional
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

    @Transactional
    public RolDTO actualizar(String codigo, RolDTO dto) {
        Rol rol = obtenerRol(codigo, true);
        validarRetiroHabilitaciones(rol, dto);

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            String nombreNormalizado = NormalizadorTexto.normalizarConstante(dto.getNombre());

            if (!rol.getNombre().equals(nombreNormalizado)) {
                // La baja de un usuario no permite cambiar indirectamente su rol.
                if (usuarioRepository.existsByRol_Id(rol.getId())) {
                    throw new OperacionNoPermitidaException("No se puede renombrar un rol con usuarios asociados, activos o inactivos.");
                }
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

        if (dto.getPermisosPorEntidad() != null) {
            asignarPermisos(rol, dto.getPermisosPorEntidad());
        }

        if (dto.getHabilitadoParaDefensa() != null) {
            rol.setHabilitadoParaDefensa(dto.getHabilitadoParaDefensa());
        }
        if (dto.getHabilitadoParaVigilancia() != null) {
            rol.setHabilitadoParaVigilancia(dto.getHabilitadoParaVigilancia());
        }

        return convertirADTO(rolRepository.save(rol));
    }

    public RolDTO eliminar(String codigo) {
        Rol rol = obtenerRol(codigo);

        if (!rol.getActivo()) {
            throw new RolYaInactivoException();
        }

        if (usuarioRepository.existsByRol_IdAndActivoTrue(rol.getId())) {
            throw new OperacionNoPermitidaException("No se puede dar de baja un rol con usuarios activos asignados");
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
        dto.setHabilitadoParaDefensa(rol.isHabilitadoParaDefensa());
        dto.setHabilitadoParaVigilancia(rol.isHabilitadoParaVigilancia());

        Set<RolPermisoEntidadDTO> permisos = new HashSet<>();

        for (RolPermisoEntidad relacion : rol.getPermisosPorEntidad()) {
            RolPermisoEntidadDTO permisoDTO = new RolPermisoEntidadDTO();
            permisoDTO.setPermiso(relacion.getPermiso());
            permisoDTO.setEntidad(relacion.getEntidad());
            permisos.add(permisoDTO);
        }

        dto.setPermisosPorEntidad(permisos);
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
        rol.setHabilitadoParaDefensa(Boolean.TRUE.equals(dto.getHabilitadoParaDefensa()));
        rol.setHabilitadoParaVigilancia(Boolean.TRUE.equals(dto.getHabilitadoParaVigilancia()));

        asignarPermisos(rol, dto.getPermisosPorEntidad());

        return rol;
    }

    // --- Métodos Auxiliares ---
    private Rol obtenerRol(String codigo) {
        return obtenerRol(codigo, false);
    }

    private Rol obtenerRol(String codigo, boolean bloquear) {
        String codigoNormalizado = codigo != null ? codigo.trim().toUpperCase() : null;

        Optional<Rol> resultado;
        if (bloquear) {
            resultado = rolRepository.findConBloqueoByCodigo(codigoNormalizado);
        } else {
            resultado = rolRepository.findByCodigo(codigoNormalizado);
        }

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Rol", "código: " + codigo);
        }

        return resultado.get();
    }

    private void validarRetiroHabilitaciones(Rol rol, RolDTO dto) {
        if (rol.isHabilitadoParaDefensa() && Boolean.FALSE.equals(dto.getHabilitadoParaDefensa())
                && asaltanteRepository.existsByAbogadoDefensor_Rol_Id(rol.getId())) {
            throw new OperacionNoPermitidaException("No se puede retirar la habilitación de defensa: hay usuarios del rol con defendidos asignados.");
        }
        if (rol.isHabilitadoParaVigilancia() && Boolean.FALSE.equals(dto.getHabilitadoParaVigilancia())
                && contratoVigilanciaRepository.existsByVigilante_Rol_IdAndFechaGreaterThanEqualAndActivoTrue(rol.getId(), LocalDate.now())) {
            throw new OperacionNoPermitidaException("No se puede retirar la habilitación de vigilancia: hay usuarios del rol con contratos activos de hoy o futuros.");
        }
    }

    private void asignarPermisos(Rol rol, Set<RolPermisoEntidadDTO> permisosDTO) {
        if (permisosDTO == null || permisosDTO.isEmpty()) {
            throw new OperacionNoPermitidaException("El rol debe tener al menos un permiso");
        }
        List<RolPermisoEntidad> permisos = new ArrayList<>();
        for (RolPermisoEntidadDTO dto : permisosDTO) {
            if (dto == null) {
                throw new OperacionNoPermitidaException("El permiso y la entidad son obligatorios");
            }
            permisos.add(new RolPermisoEntidad(rol, dto.getPermiso(), dto.getEntidad()));
        }
        rol.actualizarPermisos(permisos);
    }
}
