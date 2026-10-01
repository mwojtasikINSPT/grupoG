package prog2.policia_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import prog2.policia_backend.DTOs.BandaDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Banda;
import prog2.policia_backend.repositories.BandaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BandaService {

    private final BandaRepository bandaRepository;

    public List<BandaDTO> listar() {
        return bandaRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public BandaDTO buscarPorId(Long id) {
        return bandaRepository.findById(id)
                .filter(Banda::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Banda", id));
    }

    public BandaDTO guardar(BandaDTO dto) {
        Banda banda = convertirAEntidad(dto);
        return convertirADTO(bandaRepository.save(banda));
    }

    public BandaDTO actualizar(Long id, BandaDTO dto) {
        Banda banda = bandaRepository.findById(id)
                .filter(Banda::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Banda", id));

        banda.setCantMiembros(dto.getCantMiembros());

        return convertirADTO(bandaRepository.save(banda));
    }

    public void eliminar(Long id) {
        Banda banda = bandaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Banda", id));

        banda.setActivo(false);
        bandaRepository.save(banda);
    }

    private BandaDTO convertirADTO(Banda banda) {
        return new BandaDTO(
                banda.getId(),
                banda.getCantMiembros(),
                banda.getCodigo()
        );
    }

    private Banda convertirAEntidad(BandaDTO dto) {
        Banda banda = new Banda();
        banda.setCantMiembros(dto.getCantMiembros());

        return banda;
    }
}
