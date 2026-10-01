package prog2.policia_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import prog2.policia_backend.DTOs.InvestigadorDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Investigador;
import prog2.policia_backend.repositories.InvestigadorRepository;

import java.util.List;
import prog2.policia_backend.utils.GeneradorCodigo;

@Service
@RequiredArgsConstructor
public class InvestigadorService {

    private final InvestigadorRepository investigadorRepository;

    public List<InvestigadorDTO> listar() {
        return investigadorRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public InvestigadorDTO buscarPorId(Long id) {
        return investigadorRepository.findById(id)
                .filter(Investigador::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Investigador", id));
    }

    public InvestigadorDTO guardar(InvestigadorDTO dto) {
        Investigador investigador = convertirAEntidad(dto);

        investigador = investigadorRepository.save(investigador);

        investigador.setCodigo(
                GeneradorCodigo.generar("INV", investigador.getId())
        );

        return convertirADTO(investigadorRepository.save(investigador));
    }

    public InvestigadorDTO actualizar(Long id, InvestigadorDTO dto) {
        Investigador investigador = investigadorRepository.findById(id)
                .filter(Investigador::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Investigador", id));

        investigador.setNombre(dto.getNombre());

        return convertirADTO(investigadorRepository.save(investigador));
    }

    public void eliminar(Long id) {
        Investigador investigador = investigadorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Investigador", id));

        investigador.setActivo(false);
        investigadorRepository.save(investigador);
    }

    private InvestigadorDTO convertirADTO(Investigador investigador) {
        return new InvestigadorDTO(
                investigador.getId(),
                investigador.getCodigo(),
                investigador.getNombre()
        );
    }

    private Investigador convertirAEntidad(InvestigadorDTO dto) {
        Investigador investigador = new Investigador();

        investigador.setCodigo(dto.getCodigo());
        investigador.setNombre(dto.getNombre());

        return investigador;
    }
}
