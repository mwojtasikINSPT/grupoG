package prog2.policia_backend.services;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prog2.policia_backend.DTOs.PresentacionPruebaDefensaDTO;
import prog2.policia_backend.DTOs.PruebaDefensaDTO;
import prog2.policia_backend.exceptions.CasoCerradoParaPruebaException;
import prog2.policia_backend.models.CasoJudicial;
import prog2.policia_backend.models.EstadoCasoJudicial;
import prog2.policia_backend.models.PruebaDefensa;
import prog2.policia_backend.models.SentenciaAsaltante;
import prog2.policia_backend.models.Usuario;
import prog2.policia_backend.repositories.PruebaDefensaRepository;

@Service
@RequiredArgsConstructor
public class PruebaDefensaService {
    private final PruebaDefensaRepository pruebaDefensaRepository;
    private final AccesoDefensaService accesoDefensaService;

    @Transactional
    @PreAuthorize("hasAuthority('ESCRITURA_PRUEBA_DEFENSA')")
    public PruebaDefensaDTO presentar(PresentacionPruebaDefensaDTO dto) {
        SentenciaAsaltante participacion = accesoDefensaService
                .obtenerParticipacionParaPresentacion(dto.getAsaltanteCodigo(), dto.getCasoCodigo());
        CasoJudicial caso = participacion.getCasoJudicial();
        if (!caso.isActivo() || caso.getEstado() == EstadoCasoJudicial.CERRADO) {
            throw new CasoCerradoParaPruebaException();
        }
        Usuario abogado = accesoDefensaService.obtenerAbogadoAutenticado();
        PruebaDefensa prueba = new PruebaDefensa(dto.getDescripcion().trim(), abogado,
                participacion.getAsaltante(), caso);
        return convertirADTO(pruebaDefensaRepository.save(prueba));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('LECTURA_PRUEBA_DEFENSA', 'LECTURA_DATOS_PROPIOS_PRUEBA_DEFENSA')")
    public List<PruebaDefensaDTO> consultar(String asaltanteCodigo, String casoCodigo) {
        SentenciaAsaltante participacion = accesoDefensaService
                .obtenerParticipacionParaConsultaPruebas(asaltanteCodigo, casoCodigo);
        List<PruebaDefensaDTO> resultado = new ArrayList<>();
        for (PruebaDefensa prueba : pruebaDefensaRepository
                .findByAsaltante_IdAndCasoJudicial_IdOrderByFechaPresentacionAscIdAsc(
                        participacion.getAsaltante().getId(), participacion.getCasoJudicial().getId())) {
            resultado.add(convertirADTO(prueba));
        }
        return resultado;
    }

    private PruebaDefensaDTO convertirADTO(PruebaDefensa prueba) {
        return new PruebaDefensaDTO(prueba.getId(), prueba.getDescripcion(), prueba.getFechaPresentacion(),
                prueba.getAbogado().getCodigo(), prueba.getAsaltante().getCodigo(), prueba.getCasoJudicial().getCodigo());
    }
}
