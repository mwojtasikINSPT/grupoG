package prog2.policia_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import prog2.policia_backend.DTOs.VigilanteDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Vigilante;
import prog2.policia_backend.repositories.VigilanteRepository;

import java.util.List;
import prog2.policia_backend.utils.GeneradorCodigo;

@Service
@RequiredArgsConstructor
public class VigilanteService {

    private final VigilanteRepository vigilanteRepository;

    public List<VigilanteDTO> listar() {
        return vigilanteRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public VigilanteDTO buscarPorId(Long id) {
        return vigilanteRepository.findById(id)
                .filter(Vigilante::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vigilante", id));
    }

    public VigilanteDTO guardar(VigilanteDTO dto) {
        Vigilante vigilante = convertirAEntidad(dto);

        vigilante = vigilanteRepository.save(vigilante);

        vigilante.setCodigo(
                GeneradorCodigo.generar("VIG", vigilante.getId())
        );

        return convertirADTO(vigilanteRepository.save(vigilante));
    }

    public VigilanteDTO actualizar(Long id, VigilanteDTO dto) {
        Vigilante vigilante = vigilanteRepository.findById(id)
                .filter(Vigilante::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vigilante", id));

        vigilante.setNombre(dto.getNombre());
        vigilante.setPassword(dto.getPassword());
        vigilante.setEdad(dto.getEdad());

        return convertirADTO(vigilanteRepository.save(vigilante));
    }

    public void eliminar(Long id) {
        Vigilante vigilante = vigilanteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vigilante", id));

        vigilante.setActivo(false);
        vigilanteRepository.save(vigilante);
    }

    private VigilanteDTO convertirADTO(Vigilante vigilante) {
        return new VigilanteDTO(
                vigilante.getId(),
                vigilante.getCodigo(),
                vigilante.getNombre(),
                vigilante.getPassword(),
                vigilante.getEdad()
        );
    }

    private Vigilante convertirAEntidad(VigilanteDTO dto) {
        Vigilante vigilante = new Vigilante();

        vigilante.setCodigo(dto.getCodigo());
        vigilante.setNombre(dto.getNombre());
        vigilante.setPassword(dto.getPassword());
        vigilante.setEdad(dto.getEdad());

        return vigilante;
    }
}
