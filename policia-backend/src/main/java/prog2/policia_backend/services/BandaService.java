package prog2.policia_backend.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.BandaDTO;
import prog2.policia_backend.exceptions.BandaConMiembrosException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Banda;
import prog2.policia_backend.repositories.BandaRepository;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.utils.GeneradorCodigo;

@Service
@RequiredArgsConstructor
public class BandaService {

    private final BandaRepository bandaRepository;
    private final AsaltanteRepository asaltanteRepository;

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

    public BandaDTO buscarPorCodigo(String codigo) {
        return bandaRepository.findByCodigo(codigo)
                .filter(Banda::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Banda", codigo));
    }

    public BandaDTO guardar() {
        Banda banda = new Banda();

        banda = bandaRepository.save(banda);

        banda.setCodigo(
                GeneradorCodigo.generar("BAN", banda.getId()));

        banda = bandaRepository.save(banda);

        return convertirADTO(banda);
    }

    /*
    //Por el momento, no se usa para nada
    public BandaDTO actualizar(Long id, BandaDTO dto) {
        Banda banda = bandaRepository.findById(id)
                .filter(Banda::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Banda", id));
        return convertirADTO(bandaRepository.save(banda));
    }

     */
    public void eliminar(String codigo) {
        Banda banda = bandaRepository.findByCodigo(codigo)
                .filter(Banda::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Banda", codigo));

        if (asaltanteRepository.existsByBanda_IdAndActivoTrue(banda.getId())) {
            throw new BandaConMiembrosException();
        }

        banda.setActivo(false);
        bandaRepository.save(banda);
    }

    public BandaDTO reactivar(String codigo) {
        Banda banda = bandaRepository.findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Banda", codigo));

        banda.setActivo(true);

        banda = bandaRepository.save(banda);

        return convertirADTO(banda);
    }

    private BandaDTO convertirADTO(Banda banda) {
        return new BandaDTO(
                banda.getId(),
                banda.getCantMiembros(),
                banda.getCodigo()
        );
    }

}
