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
import prog2.policia_backend.exceptions.CasoJudicialActivoException;
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

    public CasoJudicialDTO buscarPorCodigo(String codigo) {
        return convertirADTO(obtenerCaso(codigo));
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

        caso.setSentenciado(false);
        caso.setCondenado(false);
        caso.setTiempoCarcel(0);
        caso.setActivo(true);
        caso = casoJudicialRepository.save(caso);
        caso.setCodigo(GeneradorCodigo.generar("CJU", caso.getId()));
        caso = casoJudicialRepository.save(caso);

        return convertirADTO(caso);
    }

    public CasoJudicialDTO actualizar(String codigo, CasoJudicialDTO dto) {
        CasoJudicial caso = obtenerCaso(codigo);

        if (!caso.isActivo()) {
            throw new CasoSentenciadoException();
        }

        boolean condenado = dto.getCondenado() != null ? dto.getCondenado() : caso.isCondenado();
        int tiempoCarcel = dto.getTiempoCarcel() != null ? dto.getTiempoCarcel() : caso.getTiempoCarcel();

        validarCondena(condenado, tiempoCarcel);

        caso.setCondenado(condenado);
        caso.setTiempoCarcel(tiempoCarcel);

        if (dto.getSentenciado() != null) {
            caso.setSentenciado(dto.getSentenciado());
            if (dto.getSentenciado()) {
                caso.setActivo(false); // Si hay sentencia, el caso queda cerrado
            }
        }

        if (dto.getJuezCodigo() != null) {
            Juez juez = obtenerJuezActivo(dto.getJuezCodigo());
            caso.setJuez(juez);
        }

        return convertirADTO(casoJudicialRepository.save(caso));
    }

    public List<CasoJudicialDTO> listarPorAsaltante(String codigo) {

        Asaltante asaltante = asaltanteRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asaltante", codigo));

        return casoJudicialRepository
                .findByAsaltante_Id(asaltante.getId())
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<CasoJudicialDTO> listarPorJuez(String codigo) {

        Juez juez = juezRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Juez", codigo));

        return casoJudicialRepository
                .findByJuez_Id(juez.getId())
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public void eliminar(String codigo) {
        CasoJudicial caso = obtenerCaso(codigo);

        if (!caso.isActivo()) {
            throw new CasoSentenciadoException(); //ver poner una exc nueva
        }
        caso.setActivo(false);
        casoJudicialRepository.save(caso);
    }

    public CasoJudicialDTO reactivar(String codigo) {
        CasoJudicial caso = obtenerCaso(codigo);

        if (caso.isActivo()) {
            throw new CasoJudicialActivoException();
        }

        caso.setActivo(true);
        caso.setSentenciado(false); // Se reabre el caso, por lo que deja de estar sentenciado de forma definitiva

        return convertirADTO(casoJudicialRepository.save(caso));
    }

    private CasoJudicialDTO convertirADTO(CasoJudicial caso) {
        CasoJudicialDTO dto = new CasoJudicialDTO();

        dto.setId(caso.getId());
        dto.setCodigo(caso.getCodigo());
        dto.setCondenado(caso.isCondenado());
        dto.setSentenciado(caso.isSentenciado());
        dto.setTiempoCarcel(caso.getTiempoCarcel());
        dto.setActivo(caso.isActivo());

        if (caso.getAsalto() != null) {
            dto.setAsaltoCodigo(caso.getAsalto().getCodigo());
        }

        if (caso.getAsaltante() != null) {
            dto.setAsaltanteCodigo(caso.getAsaltante().getCodigo());
        }

        if (caso.getJuez() != null) {
            dto.setJuezCodigo(caso.getJuez().getCodigo());
        }

        dto.setFechaCreacion(caso.getFechaCreacion());
        dto.setFechaModificacion(caso.getFechaModificacion());
        dto.setCreadoPor(caso.getCreadoPor());
        dto.setModificadoPor(caso.getModificadoPor());

        return dto;
    }

    private CasoJudicial convertirAEntidad(Asalto asalto, Asaltante asaltante, Juez juez) {
        CasoJudicial caso = new CasoJudicial();
        caso.setAsalto(asalto);
        caso.setAsaltante(asaltante);
        caso.setJuez(juez);

        return caso;
    }

    // --- Métodos Auxiliares ---
    private CasoJudicial obtenerCaso(String codigo) {
        return casoJudicialRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("CasoJudicial", codigo));
    }

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
