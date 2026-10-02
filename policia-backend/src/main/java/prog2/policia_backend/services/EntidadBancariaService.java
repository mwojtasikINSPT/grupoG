package prog2.policia_backend.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.EntidadBancariaDTO;
import prog2.policia_backend.exceptions.EntidadBancariaConSucursalesException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.EntidadBancaria;
import prog2.policia_backend.repositories.EntidadBancariaRepository;
import prog2.policia_backend.repositories.SucursalRepository;
import prog2.policia_backend.utils.GeneradorCodigo;

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
                .filter(EntidadBancaria::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "EntidadBancaria", codigo));
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

        entidad.setNombre(dto.getNombre());
        entidad.setDomicilioCentral(dto.getDomicilioCentral());

        return convertirADTO(
                entidadBancariaRepository.save(entidad)
        );
    }

    // EB con SUC activa no se puede eliminar
    public void eliminar(String codigo) {

        EntidadBancaria entidadBancaria = entidadBancariaRepository
                .findByCodigo(codigo)
                .filter(EntidadBancaria::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "EntidadBancaria", codigo));

        if (sucursalRepository.existsByEntidadBancaria_IdAndActivoTrue(
                entidadBancaria.getId())) {

            throw new EntidadBancariaConSucursalesException();
        }

        entidadBancaria.setActivo(false);

        entidadBancariaRepository.save(entidadBancaria);
    }

    public EntidadBancariaDTO reactivar(String codigo) {

        EntidadBancaria entidadBancaria = entidadBancariaRepository
                .findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "EntidadBancaria", codigo));

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
                entidad.getNombre()
        );
    }

    private EntidadBancaria convertirAEntidad(
            EntidadBancariaDTO dto) {

        EntidadBancaria entidad = new EntidadBancaria();

        entidad.setDomicilioCentral(dto.getDomicilioCentral());
        entidad.setNombre(dto.getNombre());

        return entidad;
    }
}
