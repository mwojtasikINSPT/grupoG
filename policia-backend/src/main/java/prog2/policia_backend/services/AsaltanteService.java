package prog2.policia_backend.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.AsaltanteDTO;
import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.BandaRepository;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Banda;
import prog2.policia_backend.models.MotivoBajaPersona;
import prog2.policia_backend.utils.GeneradorCodigo;

@Service
@RequiredArgsConstructor
public class AsaltanteService {

    private final AsaltanteRepository asaltanteRepository;
    private final BandaRepository bandaRepository;

    public List<AsaltanteDTO> listar(Boolean activo) {

        List<Asaltante> asaltantes;

        if (activo == null) {
            asaltantes = asaltanteRepository.findAll();
        } else {
            asaltantes = asaltanteRepository.findByActivo(activo);
        }

        return asaltantes.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public AsaltanteDTO buscarPorId(Long id) {
        return asaltanteRepository.findById(id)
                .filter(Asaltante::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asaltante", id));
    }

    public AsaltanteDTO buscarPorCodigo(String codigo) {
        return asaltanteRepository.findByCodigo(codigo)
                .filter(Asaltante::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Asaltante", codigo));
    }

    public AsaltanteDTO guardar(AsaltanteDTO dto) {
        Asaltante asaltante = convertirAEntidad(dto);

        asaltante = asaltanteRepository.save(asaltante);

        asaltante.setCodigo(
                GeneradorCodigo.generar("ASS", asaltante.getId())
        );

        asaltante = asaltanteRepository.save(asaltante);

        return convertirADTO(asaltante);
    }

    public AsaltanteDTO actualizar(String codigo, AsaltanteDTO dto) {
        Asaltante asaltante = asaltanteRepository.findByCodigo(codigo)
                .filter(Asaltante::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Asaltante", codigo));

        asaltante.setNombre(dto.getNombre());

        if (dto.getBandaId() != null) {
            Banda banda = bandaRepository.findById(dto.getBandaId())
                    .filter(Banda::isActivo)
                    .orElseThrow(()
                            -> new RecursoNoEncontradoException(
                            "Banda", dto.getBandaId()));

            asaltante.setBanda(banda);
        } else {
            asaltante.setBanda(null);
        }

        asaltante = asaltanteRepository.save(asaltante);

        return convertirADTO(asaltante);
    }

    public void eliminar(String codigo) {
        Asaltante asaltante = asaltanteRepository.findByCodigo(codigo)
                .filter(Asaltante::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Asaltante", codigo));

        asaltante.setActivo(false);
        asaltante.setMotivoBaja(MotivoBajaPersona.FALLECIMIENTO);
        asaltanteRepository.save(asaltante);
    }

    private AsaltanteDTO convertirADTO(Asaltante asaltante) {
        return new AsaltanteDTO(
                asaltante.getId(),
                asaltante.getNombre(),
                asaltante.getBanda() != null ? asaltante.getBanda().getId() : null,
                asaltante.getCodigo(),
                asaltante.getMotivoBaja(),
                asaltante.getFechaCreacion(),
                asaltante.getFechaModificacion(),
                asaltante.getCreadoPor(),
                asaltante.getModificadoPor()
        );
    }

    // Convierto un AsaltanteDTO en una entidad Asaltante para el Repo
    private Asaltante convertirAEntidad(AsaltanteDTO dto) {
        Asaltante asaltante = new Asaltante();

        asaltante.setNombre(dto.getNombre());

        if (dto.getBandaId() != null) {
            asaltante.setBanda(
                    bandaRepository.findById(dto.getBandaId())
                            .filter(Banda::isActivo)
                            .orElseThrow(()
                                    -> new RecursoNoEncontradoException(
                                    "Banda", dto.getBandaId()))
            );
        }

        return asaltante;
    }

}
