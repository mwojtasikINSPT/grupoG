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

        entidad = entidadBancariaRepository.save(entidad);

        entidad.setCodigo(
                GeneradorCodigo.generar("EBA", entidad.getId())
        );

        entidad = entidadBancariaRepository.save(entidad);

        return convertirADTO(entidad);
    }

    public EntidadBancariaDTO actualizar(Long id, EntidadBancariaDTO dto) {
        EntidadBancaria entidad = entidadBancariaRepository.findById(id)
                .filter(EntidadBancaria::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("EntidadBancaria", id));

        entidad.setDomicilioCentral(dto.getDomicilioCentral());

        return convertirADTO(entidadBancariaRepository.save(entidad));
    }

    //EB con SUC no se puede eliminar
    public void eliminar(Long id) {

        EntidadBancaria entidadBancaria = entidadBancariaRepository.findById(id)
                .filter(EntidadBancaria::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("EntidadBancaria", id));

        if (sucursalRepository.existsByEntidadBancaria_IdAndActivoTrue(id)) {
            throw new EntidadBancariaConSucursalesException();
        }

        entidadBancaria.setActivo(false);
        entidadBancariaRepository.save(entidadBancaria);
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
        entidad.setNombre(dto.getNombre());

        return entidad;
    }
}
