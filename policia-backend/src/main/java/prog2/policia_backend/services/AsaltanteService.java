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
import prog2.policia_backend.utils.NormalizadorTexto;

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

    public AsaltanteDTO buscarPorCodigo(String codigo) {
        return convertirADTO(obtenerAsaltante(codigo));
    }

    public List<AsaltanteDTO> buscarPorNombre(String nombre, Boolean activo) {

        String nombreNormalizado = NormalizadorTexto.normalizarParaBuscar(nombre);

        return asaltanteRepository.findAll()
                .stream()
                .filter(asaltante -> activo == null || asaltante.isActivo() == activo)
                .filter(asaltante -> NormalizadorTexto.normalizarParaBuscar(asaltante.getNombre())
                .contains(nombreNormalizado))
                .map(this::convertirADTO)
                .toList();
    }

    public AsaltanteDTO guardar(AsaltanteDTO dto) {
        Asaltante asaltante = convertirAEntidad(dto);

        asaltante = asaltanteRepository.save(asaltante);

        asaltante.setCodigo(GeneradorCodigo.generar("ASS", asaltante.getId()));
        asaltante.setActivo(true);

        asaltante = asaltanteRepository.save(asaltante);

        return convertirADTO(asaltante);
    }

    public AsaltanteDTO actualizar(String codigo, AsaltanteDTO dto) {

        Asaltante asaltante = obtenerAsaltante(codigo);

        if (!asaltante.isActivo()) {
            throw new PersonaInactivaException();
        }

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            asaltante.setNombre(NormalizadorTexto.normalizarParaGuardar(dto.getNombre()));
        }

        if (Boolean.TRUE.equals(dto.getQuitarDeBanda())) {
            asaltante.setBanda(null);
        } else if (dto.getBandaCodigo() != null && !dto.getBandaCodigo().isBlank()) {
            asaltante.setBanda(obtenerBandaActiva(dto.getBandaCodigo()));
        }

        asaltante = asaltanteRepository.save(asaltante);

        return convertirADTO(asaltante);
    }

    public void eliminar(String codigo, AsaltanteDTO dto) {
        Asaltante asaltante = obtenerAsaltante(codigo);

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
        asaltante.setMotivoBaja(dto.getMotivoBaja());
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
                asaltante.isActivo(),
                asaltante.getFechaCreacion(),
                asaltante.getFechaModificacion(),
                asaltante.getCreadoPor(),
                asaltante.getModificadoPor()
        );
    }

    // Convierto un AsaltanteDTO en una entidad Asaltante para el Repo
    private Asaltante convertirAEntidad(AsaltanteDTO dto) {
        Asaltante asaltante = new Asaltante();

        asaltante.setNombre(NormalizadorTexto.normalizarParaGuardar(dto.getNombre()));

        if (dto.getBandaCodigo() != null) {
            asaltante.setBanda(obtenerBandaActiva(dto.getBandaCodigo()));
        }

        asaltante.setActivo(dto.getActivo() == null || dto.getActivo());

        return asaltante;
    }

    //----Métodos Aux------
    private Asaltante obtenerAsaltante(String codigo) {

        return asaltanteRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asaltante", codigo));
    }

    private Banda obtenerBandaActiva(String bandaCodigo) {
        Banda banda = bandaRepository.findByCodigo(bandaCodigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Banda", bandaCodigo));

        if (!banda.isActivo()) {
            throw new BandaInactivaException();
        }

        return banda;
    }
}
