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
import prog2.policia_backend.exceptions.CasoSentenciadoException;
import prog2.policia_backend.exceptions.CasoSinCondenaConCarcelException;
import prog2.policia_backend.exceptions.PersonaInactivaException;
import prog2.policia_backend.models.Asalto;
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

        Asalto asalto = obtenerAsalto(dto.getAsaltoCodigo());
        Asaltante asaltante = obtenerAsaltanteActivo(dto.getAsaltanteCodigo());
        Juez juez = obtenerJuezActivo(dto.getJuezCodigo());

        if (casoJudicialRepository.existsByAsalto_IdAndAsaltante_Id(asalto.getId(), asaltante.getId())) {
            throw new CasoJudicialYaExistenteException();
        }

        if (!asaltoRepository.existsByIdAndAsaltantes_Id(asalto.getId(), asaltante.getId())) {
            throw new AsaltanteNoParticipaEnAsaltoException();
        }

        CasoJudicial caso = convertirAEntidad(asalto, asaltante, juez);

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
                .orElseThrow(() -> new RecursoNoEncontradoException("CasoJudicial", codigo));

        if (!caso.isActivo()) {
            throw new CasoSentenciadoException();
        }

        validarCondena(dto.isCondenado(), dto.getTiempoCarcel());

        Juez juez = obtenerJuezActivo(dto.getJuezCodigo());

        caso.setCondenado(dto.isCondenado());
        caso.setTiempoCarcel(dto.getTiempoCarcel());
        caso.setJuez(juez);
        caso.setSentenciado(dto.isSentenciado());

        if (dto.isSentenciado()) {
            caso.setActivo(false);
        }

        return convertirADTO(casoJudicialRepository.save(caso));
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

    public List<CasoJudicialDTO> listarPorJuez(String codigo) {

        Juez juez = juezRepository.findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "Juez", codigo));

        return casoJudicialRepository
                .findByJuez_Id(juez.getId())
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
                caso.getAsalto().getCodigo(),
                caso.getAsaltante().getCodigo(),
                caso.getJuez().getCodigo(),
                caso.getCodigo(),
                caso.getFechaCreacion(),
                caso.getFechaModificacion(),
                caso.getCreadoPor(),
                caso.getModificadoPor()
        );
    }

    private CasoJudicial convertirAEntidad(Asalto asalto, Asaltante asaltante, Juez juez) {
        CasoJudicial caso = new CasoJudicial();
        caso.setAsalto(asalto);
        caso.setAsaltante(asaltante);
        caso.setJuez(juez);
        return caso;
    }

    // --- Métodos Auxiliares ---
    private Asalto obtenerAsalto(String codigo) {
        return asaltoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asalto", codigo));
    }

    private Asaltante obtenerAsaltanteActivo(String codigo) {
        Asaltante asaltante = asaltanteRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asaltante", codigo));
        if (!asaltante.isActivo()) {
            throw new PersonaInactivaException();
        }
        return asaltante;
    }

    private Juez obtenerJuezActivo(String codigo) {
        Juez juez = juezRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Juez", codigo));
        if (!juez.isActivo()) {
            throw new PersonaInactivaException();
        }
        return juez;
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
