package prog2.policia_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import prog2.policia_backend.DTOs.CasoJudicialDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Asalto;
import prog2.policia_backend.models.CasoJudicial;
import prog2.policia_backend.models.Juez;
import prog2.policia_backend.repositories.AsaltoRepository;
import prog2.policia_backend.repositories.CasoJudicialRepository;
import prog2.policia_backend.repositories.JuezRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CasoJudicialService {

    private final CasoJudicialRepository casoJudicialRepository;
    private final AsaltoRepository asaltoRepository;
    private final JuezRepository juezRepository;

    public List<CasoJudicialDTO> listar() {
        return casoJudicialRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public CasoJudicialDTO buscarPorId(Long id) {
        return casoJudicialRepository.findById(id)
                .filter(CasoJudicial::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("CasoJudicial", id));
    }

    public CasoJudicialDTO guardar(CasoJudicialDTO dto) {
        CasoJudicial caso = convertirAEntidad(dto);
        return convertirADTO(casoJudicialRepository.save(caso));
    }

    public CasoJudicialDTO actualizar(Long id, CasoJudicialDTO dto) {
        CasoJudicial caso = casoJudicialRepository.findById(id)
                .filter(CasoJudicial::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("CasoJudicial", id));

        caso.setCondenado(dto.isCondenado());
        caso.setTiempoCarcel(dto.getTiempoCarcel());

        Asalto asalto = asaltoRepository.findById(dto.getAsaltoId())
                .filter(Asalto::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Asalto", dto.getAsaltoId()));

        Juez juez = juezRepository.findById(dto.getJuezId())
                .filter(Juez::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Juez", dto.getJuezId()));

        caso.setAsalto(asalto);
        caso.setJuez(juez);

        return convertirADTO(casoJudicialRepository.save(caso));
    }

    public void eliminar(Long id) {
        CasoJudicial caso = casoJudicialRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("CasoJudicial", id));

        caso.setActivo(false);
        casoJudicialRepository.save(caso);
    }

    private CasoJudicialDTO convertirADTO(CasoJudicial caso) {
        return new CasoJudicialDTO(
                caso.getId(),
                caso.isCondenado(),
                caso.getTiempoCarcel(),
                caso.getAsalto().getId(),
                caso.getAsaltante().getId(),
                caso.getJuez().getId(),
                caso.getCodigo()
        );
    }

    private CasoJudicial convertirAEntidad(CasoJudicialDTO dto) {
    CasoJudicial caso = new CasoJudicial();

    caso.setCondenado(dto.isCondenado());
    caso.setTiempoCarcel(dto.getTiempoCarcel());

    caso.setAsalto(
            asaltoRepository.findById(dto.getAsaltoId())
                    .filter(Asalto::isActivo)
                    .orElseThrow(() ->
                            new RecursoNoEncontradoException(
                                    "Asalto", dto.getAsaltoId()))
    );

    caso.setJuez(
            juezRepository.findById(dto.getJuezId())
                    .filter(Juez::isActivo)
                    .orElseThrow(() ->
                            new RecursoNoEncontradoException(
                                    "Juez", dto.getJuezId()))
    );

    return caso;
}
}
