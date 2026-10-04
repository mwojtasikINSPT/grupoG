package prog2.policia_backend.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.EntidadBancariaDTO;
import prog2.policia_backend.exceptions.EntidadBancariaConSucursalesException;
import prog2.policia_backend.exceptions.EntidadYaActivaException;
import prog2.policia_backend.exceptions.EntidadYaInactivaException;
import prog2.policia_backend.exceptions.MotivoBajaObligatorioException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.EntidadBancaria;
import prog2.policia_backend.repositories.EntidadBancariaRepository;
import prog2.policia_backend.repositories.SucursalRepository;
import prog2.policia_backend.utils.GeneradorCodigo;
import prog2.policia_backend.utils.NormalizadorTexto;

@Service
@RequiredArgsConstructor
public class EntidadBancariaService {

    private final EntidadBancariaRepository entidadBancariaRepository;
    private final SucursalRepository sucursalRepository;

    public List<EntidadBancariaDTO> listar(Boolean activo) {

        List<EntidadBancaria> entidades;

        if (activo == null) {
            entidades = entidadBancariaRepository.findAll();
        } else {
            entidades = entidadBancariaRepository.findByActivo(activo);
        }

        return entidades.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public EntidadBancariaDTO buscarPorCodigo(String codigo) {
        return entidadBancariaRepository.findByCodigo(codigo)
                //.filter(EntidadBancaria::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "EntidadBancaria", codigo));
    }

    public List<EntidadBancariaDTO> buscarPorDomicilio(
            String domicilio,
            Boolean activo) {

        String domicilioNormalizado
                = NormalizadorTexto.normalizarParaBuscar(domicilio);

        return entidadBancariaRepository.findAll()
                .stream()
                .filter(entidad
                        -> activo == null
                || entidad.isActivo() == activo)
                .filter(entidad
                        -> NormalizadorTexto.normalizarParaBuscar(
                        entidad.getDomicilioCentral()
                ).contains(domicilioNormalizado))
                .map(this::convertirADTO)
                .toList();
    }

    public List<EntidadBancariaDTO> buscarPorNombre(
            String nombre,
            Boolean activo) {

        String nombreNormalizado
                = NormalizadorTexto.normalizarParaBuscar(nombre);

        return entidadBancariaRepository.findAll()
                .stream()
                .filter(entidad
                        -> activo == null
                || entidad.isActivo() == activo)
                .filter(entidad
                        -> NormalizadorTexto.normalizarParaBuscar(
                        entidad.getNombre()
                ).contains(nombreNormalizado))
                .map(this::convertirADTO)
                .toList();
    }

    public EntidadBancariaDTO guardar(EntidadBancariaDTO dto) {
        EntidadBancaria entidad = convertirAEntidad(dto);

        entidad = entidadBancariaRepository.save(entidad);

        entidad.setCodigo(
                GeneradorCodigo.generar("EBA", entidad.getId())
        );

        entidad = entidadBancariaRepository.save(entidad);

        return convertirADTO(entidad);
    }

    public EntidadBancariaDTO actualizar(
            String codigo,
            EntidadBancariaDTO dto) {

        EntidadBancaria entidad = entidadBancariaRepository
                .findByCodigo(codigo)
                .filter(EntidadBancaria::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "EntidadBancaria", codigo));

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            entidad.setNombre(
                    NormalizadorTexto.normalizarParaGuardar(dto.getNombre())
            );
        }

        if (dto.getDomicilioCentral() != null
                && !dto.getDomicilioCentral().isBlank()) {
            entidad.setDomicilioCentral(dto.getDomicilioCentral());
        }

        return convertirADTO(
                entidadBancariaRepository.save(entidad)
        );
    }

    // EB con SUC activa no se puede eliminar
    public void eliminar(String codigo, EntidadBancariaDTO dto) {

        EntidadBancaria entidadBancaria = entidadBancariaRepository
                .findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "EntidadBancaria", codigo));

        if (!entidadBancaria.isActivo()) {
            throw new EntidadYaInactivaException();
        }

        if (dto.getMotivoBaja() == null) {
            throw new MotivoBajaObligatorioException();
        }

        if (sucursalRepository.existsByEntidadBancaria_IdAndActivoTrue(
                entidadBancaria.getId())) {

            throw new EntidadBancariaConSucursalesException();
        }

        entidadBancaria.setActivo(false);
        entidadBancaria.setMotivoBaja(dto.getMotivoBaja());

        entidadBancariaRepository.save(entidadBancaria);
    }

    public EntidadBancariaDTO reactivar(String codigo) {

        EntidadBancaria entidadBancaria = entidadBancariaRepository
                .findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "EntidadBancaria", codigo));

        if (entidadBancaria.isActivo()) {
            throw new EntidadYaActivaException();
        }

        entidadBancaria.setActivo(true);

        entidadBancaria = entidadBancariaRepository.save(entidadBancaria);

        return convertirADTO(entidadBancaria);
    }

    private EntidadBancariaDTO convertirADTO(
            EntidadBancaria entidad) {

        return new EntidadBancariaDTO(
                entidad.getId(),
                entidad.getDomicilioCentral(),
                entidad.getCodigo(),
                entidad.getNombre(),
                entidad.getMotivoBaja(),
                entidad.getFechaCreacion(),
                entidad.getFechaModificacion(),
                entidad.getCreadoPor(),
                entidad.getModificadoPor()
        );
    }

    private EntidadBancaria convertirAEntidad(
            EntidadBancariaDTO dto) {

        EntidadBancaria entidad = new EntidadBancaria();

        entidad.setDomicilioCentral(dto.getDomicilioCentral());
        entidad.setNombre(
                NormalizadorTexto.normalizarParaGuardar(dto.getNombre())
        );

        return entidad;
    }
}
