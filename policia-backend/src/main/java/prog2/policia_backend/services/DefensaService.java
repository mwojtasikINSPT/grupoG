package prog2.policia_backend.services;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prog2.policia_backend.DTOs.CasoDefensaDTO;
import prog2.policia_backend.DTOs.DefendidoDTO;
import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.models.CasoJudicial;
import prog2.policia_backend.models.SentenciaAsaltante;
import prog2.policia_backend.models.Usuario;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.SentenciaAsaltanteRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefensaService {
    private final AccesoDefensaService accesoDefensaService;
    private final AsaltanteRepository asaltanteRepository;
    private final SentenciaAsaltanteRepository sentenciaAsaltanteRepository;

    public List<DefendidoDTO> listarDefendidos() {
        Usuario abogado = accesoDefensaService.obtenerAbogadoAutenticado();
        List<DefendidoDTO> resultado = new ArrayList<>();
        for (Asaltante defendido : asaltanteRepository.findByAbogadoDefensor_Codigo(abogado.getCodigo())) {
            resultado.add(convertirDefendido(defendido));
        }
        return resultado;
    }

    public DefendidoDTO consultarDefendido(String codigo) {
        return convertirDefendido(accesoDefensaService.obtenerDefendidoAutorizado(codigo));
    }

    public List<CasoDefensaDTO> listarCasos(String codigo) {
        Asaltante defendido = accesoDefensaService.obtenerDefendidoParaConsultaJudicial(codigo);
        List<CasoDefensaDTO> resultado = new ArrayList<>();
        for (SentenciaAsaltante participacion : sentenciaAsaltanteRepository.findByAsaltante_Id(defendido.getId())) {
            resultado.add(convertirCaso(participacion));
        }
        return resultado;
    }

    public CasoDefensaDTO consultarCaso(String codigo, String casoCodigo) {
        return convertirCaso(accesoDefensaService.obtenerParticipacionAutorizada(codigo, casoCodigo));
    }

    private DefendidoDTO convertirDefendido(Asaltante defendido) {
        return new DefendidoDTO(defendido.getCodigo(), defendido.getNombre(), defendido.isActivo(), defendido.getMotivoBaja());
    }

    private CasoDefensaDTO convertirCaso(SentenciaAsaltante participacion) {
        CasoJudicial caso = participacion.getCasoJudicial();
        Boolean condenado = null;
        Integer tiempoCarcel = null;
        if (participacion.isSentenciado()) {
            condenado = participacion.isCondenado();
            tiempoCarcel = participacion.getTiempoCarcel();
        }
        return new CasoDefensaDTO(caso.getCodigo(), participacion.getAsaltante().getCodigo(),
                caso.getAsalto().getCodigo(), caso.getAsalto().getFecha(), caso.getJuez().getCodigo(),
                caso.isActivo(), caso.getEstado(), participacion.getEstado(),
                participacion.isSentenciado(), condenado, tiempoCarcel);
    }
}
