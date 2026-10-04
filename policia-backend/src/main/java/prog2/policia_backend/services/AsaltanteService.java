package prog2.policia_backend.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.AsaltanteDTO;
import prog2.policia_backend.exceptions.BandaInactivaException;
import prog2.policia_backend.exceptions.MotivoBajaAsaltanteInvalidoException;
import prog2.policia_backend.exceptions.MotivoBajaObligatorioException;
import prog2.policia_backend.exceptions.PersonaInactivaException;
import prog2.policia_backend.exceptions.PersonaYaInactivaException;
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
                //.filter(Asaltante::isActivo)
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
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Asaltante", codigo));

        if (!asaltante.isActivo()) {
            throw new PersonaInactivaException();
        }

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            asaltante.setNombre(dto.getNombre());
        }

        if (Boolean.TRUE.equals(dto.getQuitarDeBanda())) {
            asaltante.setBanda(null);

        } else if (dto.getBandaCodigo() != null
                && !dto.getBandaCodigo().isBlank()) {

            Banda banda = bandaRepository.findByCodigo(dto.getBandaCodigo())
                    .orElseThrow(()
                            -> new RecursoNoEncontradoException(
                            "Banda", dto.getBandaCodigo()));

            if (!banda.isActivo()) {
                throw new BandaInactivaException();
            }

            asaltante.setBanda(banda);
        }

        asaltante = asaltanteRepository.save(asaltante);

        return convertirADTO(asaltante);
    }

    public void eliminar(String codigo, AsaltanteDTO dto) {
        Asaltante asaltante = asaltanteRepository.findByCodigo(codigo)
                .filter(Asaltante::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Asaltante", codigo));

        if (!asaltante.isActivo()) {
            throw new PersonaYaInactivaException();
        }

        if (dto.getMotivoBaja() == null) {
            throw new MotivoBajaObligatorioException();
        }

        if (dto.getMotivoBaja() != MotivoBajaPersona.FALLECIMIENTO) {
            throw new MotivoBajaAsaltanteInvalidoException();
        }

        asaltante.setActivo(false);
        asaltante.setMotivoBaja(MotivoBajaPersona.FALLECIMIENTO);
        asaltanteRepository.save(asaltante);
    }

    private AsaltanteDTO convertirADTO(Asaltante asaltante) {
        return new AsaltanteDTO(
                asaltante.getId(),
                asaltante.getNombre(),
                asaltante.getBanda() != null ? asaltante.getBanda().getCodigo() : null,
                asaltante.getCodigo(),
                asaltante.getMotivoBaja(),
                null, //quitar de Banda
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

        if (dto.getBandaCodigo() != null) {
            asaltante.setBanda(
                    bandaRepository.findByCodigo(dto.getBandaCodigo())
                            .filter(Banda::isActivo)
                            .orElseThrow(()
                                    -> new RecursoNoEncontradoException(
                                    "Banda", dto.getBandaCodigo()))
            );
        }

        return asaltante;
    }

}
