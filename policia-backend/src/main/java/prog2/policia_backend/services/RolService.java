package prog2.policia_backend.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
import prog2.policia_backend.models.Entidad;
import prog2.policia_backend.models.Permiso;
import prog2.policia_backend.models.Rol;
import prog2.policia_backend.models.RolPermisoEntidad;
import prog2.policia_backend.repositories.RolRepository;
import prog2.policia_backend.repositories.UsuarioRepository;
import prog2.policia_backend.utils.GeneradorCodigo;
import prog2.policia_backend.utils.NormalizadorTexto;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

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

        if (dto.getPermisosPorEntidad() != null) {
            asignarPermisos(rol, dto.getPermisosPorEntidad());
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

        asignarPermisos(rol, dto.getPermisosPorEntidad());

        return rol;
    }

    // --- Métodos Auxiliares ---
    private Rol obtenerRol(String codigo) {
        String codigoNormalizado = codigo != null ? codigo.trim().toUpperCase() : null;

        Optional<Rol> resultado = rolRepository.findByCodigo(codigoNormalizado);

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Rol", "código: " + codigo);
        }

        return resultado.get();
    }

    private void asignarPermisos(Rol rol, Set<RolPermisoEntidadDTO> permisosDTO) {
        if (permisosDTO == null || permisosDTO.isEmpty()) {
            throw new OperacionNoPermitidaException("El rol debe tener al menos un permiso");
        }

        Set<String> combinaciones = new HashSet<>();

        for (RolPermisoEntidadDTO dto : permisosDTO) {
            if (dto == null || dto.getPermiso() == null || dto.getEntidad() == null) {
                throw new OperacionNoPermitidaException("El permiso y la entidad son obligatorios");
            }

            String clave = dto.getPermiso().name() + ":" + dto.getEntidad().name();

            if (!combinaciones.add(clave)) {
                throw new OperacionNoPermitidaException("No se permiten permisos duplicados sobre una entidad");
            }
        }

        exigirLectura(combinaciones, Entidad.USUARIO, Entidad.ROL);
        exigirLectura(combinaciones, Entidad.ASALTO, Entidad.ASALTANTE);
        exigirLectura(combinaciones, Entidad.ASALTO, Entidad.SUCURSAL);
        exigirLectura(combinaciones, Entidad.CASO_JUDICIAL, Entidad.ASALTO);
        exigirLectura(combinaciones, Entidad.CASO_JUDICIAL, Entidad.JUEZ);

        Iterator<RolPermisoEntidad> iterator = rol.getPermisosPorEntidad().iterator();

        while (iterator.hasNext()) {
            RolPermisoEntidad existente = iterator.next();
            String clave = existente.getPermiso().name() + ":" + existente.getEntidad().name();

            if (!combinaciones.contains(clave)) {
                iterator.remove();
            }
        }

        for (RolPermisoEntidadDTO dto : permisosDTO) {
            boolean existe = false;

            for (RolPermisoEntidad existente : rol.getPermisosPorEntidad()) {
                if (existente.getPermiso() == dto.getPermiso() && existente.getEntidad() == dto.getEntidad()) {
                    existe = true;
                    break;
                }
            }

            if (!existe) {
                RolPermisoEntidad relacion = new RolPermisoEntidad();
                relacion.setRol(rol);
                relacion.setPermiso(dto.getPermiso());
                relacion.setEntidad(dto.getEntidad());
                rol.getPermisosPorEntidad().add(relacion);
            }
        }
    }

    // Exige la lectura necesaria para una entidad con permiso de escritura.
    private void exigirLectura(Set<String> combinaciones, Entidad entidadEscritura, Entidad entidadLectura) {
        String escritura = Permiso.ESCRITURA.name() + ":" + entidadEscritura.name();
        String lectura = Permiso.LECTURA.name() + ":" + entidadLectura.name();
        if (combinaciones.contains(escritura) && !combinaciones.contains(lectura)) {
            throw new OperacionNoPermitidaException("La escritura de " + entidadEscritura + " requiere lectura de " + entidadLectura + ".");
        }
    }

}
