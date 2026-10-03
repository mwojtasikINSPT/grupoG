package prog2.policia_backend.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.CasoJudicialDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.CasoJudicial;
import prog2.policia_backend.models.Juez;
import prog2.policia_backend.repositories.AsaltoRepository;
import prog2.policia_backend.repositories.CasoJudicialRepository;
import prog2.policia_backend.repositories.JuezRepository;
import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.exceptions.AsaltanteNoParticipaEnAsaltoException;
import prog2.policia_backend.exceptions.CasoCondenadoSinCarcelException;
import prog2.policia_backend.exceptions.CasoJudicialYaExistenteException;
import prog2.policia_backend.exceptions.CasoSinCondenaConCarcelException;
import prog2.policia_backend.utils.GeneradorCodigo;

@Service
@RequiredArgsConstructor
public class CasoJudicialService {

    private final CasoJudicialRepository casoJudicialRepository;
    private final AsaltoRepository asaltoRepository;
    private final JuezRepository juezRepository;
    private final AsaltanteRepository asaltanteRepository;

    public List<CasoJudicialDTO> listar(Boolean activo) {

        List<CasoJudicial> casos;

        if (activo == null) {
            casos = casoJudicialRepository.findAll();
        } else {
            casos = casoJudicialRepository.findByActivo(activo);
        }

        return casos.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public CasoJudicialDTO buscarPorId(Long id) {
        return casoJudicialRepository.findById(id)
                //.filter(CasoJudicial::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("CasoJudicial", id));
    }

    public CasoJudicialDTO buscarPorCodigo(String codigo) {
        return casoJudicialRepository.findByCodigo(codigo)
                //.filter(CasoJudicial::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("CasoJudicial", codigo));
    }

    public CasoJudicialDTO guardar(CasoJudicialDTO dto) {

        if (casoJudicialRepository.existsByAsalto_IdAndAsaltante_Id(
                dto.getAsaltoId(),
                dto.getAsaltanteId())) {

            throw new CasoJudicialYaExistenteException();
        }

        if (!asaltoRepository.existsByIdAndAsaltantes_Id(
                dto.getAsaltoId(),
                dto.getAsaltanteId())) {

            throw new AsaltanteNoParticipaEnAsaltoException();
        }

        CasoJudicial caso = convertirAEntidad(dto);

        caso.setCondenado(false);
        caso.setTiempoCarcel(0);
        caso.setSentenciado(false);
        caso.setActivo(true);
        caso = casoJudicialRepository.save(caso);
        caso.setCodigo(GeneradorCodigo.generar("CJU", caso.getId()));
        caso = casoJudicialRepository.save(caso);

        return convertirADTO(caso);
    }

    public CasoJudicialDTO actualizar(String codigo, CasoJudicialDTO dto) {

        CasoJudicial caso = casoJudicialRepository.findByCodigo(codigo)
                .filter(CasoJudicial::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "CasoJudicial", codigo));

        validarCondena(
                dto.isCondenado(),
                dto.getTiempoCarcel()
        );

        Juez juez = juezRepository.findById(dto.getJuezId())
                .filter(Juez::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "Juez", dto.getJuezId()));

        caso.setCondenado(dto.isCondenado());
        caso.setTiempoCarcel(dto.getTiempoCarcel());
        caso.setJuez(juez);
        caso.setSentenciado(dto.isSentenciado());

        if (dto.isSentenciado()) {
            caso.setActivo(false);
        }

        return convertirADTO(
                casoJudicialRepository.save(caso)
        );
    }

    public List<CasoJudicialDTO> listarPorAsaltante(String codigo) {

        Asaltante asaltante = asaltanteRepository.findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "Asaltante", codigo));

        return casoJudicialRepository
                .findByAsaltante_Id(asaltante.getId())
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    /*
    public void eliminar(Long id) {
        CasoJudicial caso = casoJudicialRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("CasoJudicial", id));

        caso.setActivo(false);
        casoJudicialRepository.save(caso);
    
     */
    private CasoJudicialDTO convertirADTO(CasoJudicial caso) {
        return new CasoJudicialDTO(
                caso.getId(),
                caso.isCondenado(),
                caso.getTiempoCarcel(),
                caso.isSentenciado(),
                caso.getAsalto().getId(),
                caso.getAsaltante().getId(),
                caso.getJuez().getId(),
                caso.getCodigo(),
                caso.getFechaCreacion(),
                caso.getFechaModificacion(),
                caso.getCreadoPor(),
                caso.getModificadoPor()
        );
    }

    private CasoJudicial convertirAEntidad(CasoJudicialDTO dto) {
        CasoJudicial caso = new CasoJudicial();

        caso.setAsalto(
                asaltoRepository.findById(dto.getAsaltoId())
                        .orElseThrow(()
                                -> new RecursoNoEncontradoException(
                                "Asalto", dto.getAsaltoId()))
        );

        caso.setAsaltante(
                asaltanteRepository.findById(dto.getAsaltanteId())
                        .filter(Asaltante::isActivo)
                        .orElseThrow(()
                                -> new RecursoNoEncontradoException(
                                "Asaltante", dto.getAsaltanteId()))
        );

        caso.setJuez(
                juezRepository.findById(dto.getJuezId())
                        .filter(Juez::isActivo)
                        .orElseThrow(()
                                -> new RecursoNoEncontradoException(
                                "Juez", dto.getJuezId()))
        );

        return caso;
    }

    private void validarCondena(boolean condenado, int tiempoCarcel) {

        if (!condenado && tiempoCarcel != 0) {
            throw new CasoSinCondenaConCarcelException();
        }

        if (condenado && tiempoCarcel <= 0) {
            throw new CasoCondenadoSinCarcelException();
        }
    }
}
