package prog2.policia_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import prog2.policia_backend.DTOs.EntidadBancariaDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.EntidadBancaria;
import prog2.policia_backend.repositories.EntidadBancariaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EntidadBancariaService {

    private final EntidadBancariaRepository entidadBancariaRepository;

    public List<EntidadBancariaDTO> listar() {
        return entidadBancariaRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public EntidadBancariaDTO buscarPorId(Long id) {
        return entidadBancariaRepository.findById(id)
                .filter(EntidadBancaria::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("EntidadBancaria", id));
    }

    public EntidadBancariaDTO guardar(EntidadBancariaDTO dto) {
        EntidadBancaria entidad = convertirAEntidad(dto);
        return convertirADTO(entidadBancariaRepository.save(entidad));
    }

    public EntidadBancariaDTO actualizar(Long id, EntidadBancariaDTO dto) {
        EntidadBancaria entidad = entidadBancariaRepository.findById(id)
                .filter(EntidadBancaria::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("EntidadBancaria", id));

        entidad.setDomicilioCentral(dto.getDomicilioCentral());

        return convertirADTO(entidadBancariaRepository.save(entidad));
    }

    public void eliminar(Long id) {
        EntidadBancaria entidad = entidadBancariaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("EntidadBancaria", id));

        entidad.setActivo(false);
        entidadBancariaRepository.save(entidad);
    }

    private EntidadBancariaDTO convertirADTO(EntidadBancaria entidad) {
        return new EntidadBancariaDTO(
                entidad.getId(),
                entidad.getDomicilioCentral(),
                entidad.getCodigo(),
                entidad.getNombre()
        );
    }

    private EntidadBancaria convertirAEntidad(EntidadBancariaDTO dto) {
        EntidadBancaria entidad = new EntidadBancaria();
        entidad.setDomicilioCentral(dto.getDomicilioCentral());

        return entidad;
    }
}
