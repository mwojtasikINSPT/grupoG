package prog2.policia_backend.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import prog2.policia_backend.DTOs.ArchivoPruebaDTO;
import prog2.policia_backend.DTOs.PresentacionPruebaDefensaDTO;
import prog2.policia_backend.DTOs.PruebaDefensaDTO;
import prog2.policia_backend.exceptions.CasoCerradoParaPruebaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.ArchivoPruebaDefensa;
import prog2.policia_backend.models.CasoJudicial;
import prog2.policia_backend.models.EstadoCasoJudicial;
import prog2.policia_backend.models.PruebaDefensa;
import prog2.policia_backend.models.SentenciaAsaltante;
import prog2.policia_backend.models.Usuario;
import prog2.policia_backend.repositories.PruebaDefensaRepository;
import prog2.policia_backend.repositories.ArchivoPruebaDefensaRepository;

@Service
@RequiredArgsConstructor
public class PruebaDefensaService {
    private final PruebaDefensaRepository pruebaDefensaRepository;
    private final AccesoDefensaService accesoDefensaService;
    private final ArchivoPruebaDefensaRepository archivoPruebaDefensaRepository;
    private final ValidadorArchivoPruebaService validadorArchivoPruebaService;

    @Transactional
    @PreAuthorize("hasAuthority('ESCRITURA_PRUEBA_DEFENSA')")
    public PruebaDefensaDTO presentar(PresentacionPruebaDefensaDTO dto) {
        return presentar(dto, Collections.<MultipartFile>emptyList());
    }

    @Transactional
    @PreAuthorize("hasAuthority('ESCRITURA_PRUEBA_DEFENSA')")
    public PruebaDefensaDTO presentar(PresentacionPruebaDefensaDTO dto, List<MultipartFile> archivos) {
        SentenciaAsaltante participacion = accesoDefensaService
                .obtenerParticipacionParaPresentacion(dto.getAsaltanteCodigo(), dto.getCasoCodigo());
        CasoJudicial caso = participacion.getCasoJudicial();
        if (!caso.isActivo() || caso.getEstado() == EstadoCasoJudicial.CERRADO) {
            throw new CasoCerradoParaPruebaException();
        }
        Usuario abogado = accesoDefensaService.obtenerAbogadoAutenticado();
        // Validar antes de insertar; ambos registros pertenecen a esta transaccion.
        ArchivoPruebaDTO archivo = validadorArchivoPruebaService.validar(archivos);
        PruebaDefensa prueba = new PruebaDefensa(dto.getDescripcion().trim(), abogado,
                participacion.getAsaltante(), caso);
        prueba = pruebaDefensaRepository.save(prueba);
        if (archivo != null) {
            archivoPruebaDefensaRepository.save(new ArchivoPruebaDefensa(prueba,
                    archivo.getNombreOriginal(), archivo.getTipoContenido(), archivo.getContenido()));
        }
        return convertirADTO(prueba, archivo);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('LECTURA_PRUEBA_DEFENSA', 'LECTURA_DATOS_PROPIOS_PRUEBA_DEFENSA')")
    public List<PruebaDefensaDTO> consultar(String asaltanteCodigo, String casoCodigo) {
        SentenciaAsaltante participacion = accesoDefensaService
                .obtenerParticipacionParaConsultaPruebas(asaltanteCodigo, casoCodigo);
        return pruebaDefensaRepository.consultarMetadatos(
                participacion.getAsaltante().getId(), participacion.getCasoJudicial().getId());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('LECTURA_PRUEBA_DEFENSA', 'LECTURA_DATOS_PROPIOS_PRUEBA_DEFENSA')")
    public ArchivoPruebaDTO descargar(Long id) {
        Optional<PruebaDefensa> resultado = pruebaDefensaRepository.findById(id);
        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Prueba de defensa", id);
        }
        PruebaDefensa prueba = resultado.get();
        // Autorizar ANTES de recuperar la tabla que contiene el MEDIUMBLOB.
        accesoDefensaService.obtenerParticipacionParaConsultaPruebas(
                prueba.getAsaltante().getCodigo(), prueba.getCasoJudicial().getCodigo());
        Optional<ArchivoPruebaDefensa> adjunto = archivoPruebaDefensaRepository.findById(id);
        if (adjunto.isEmpty()) {
            throw new RecursoNoEncontradoException("Archivo adjunto de la prueba", id);
        }
        ArchivoPruebaDefensa archivo = adjunto.get();
        return new ArchivoPruebaDTO(archivo.getNombreOriginal(), archivo.getTipoContenido(), archivo.getTamano(), archivo.getContenido());
    }

    private PruebaDefensaDTO convertirADTO(PruebaDefensa prueba, ArchivoPruebaDTO archivo) {
        return new PruebaDefensaDTO(prueba.getId(), prueba.getDescripcion(), prueba.getFechaPresentacion(),
                prueba.getAbogado().getCodigo(), prueba.getAsaltante().getCodigo(), prueba.getCasoJudicial().getCodigo(),
                archivo == null ? null : archivo.getNombreOriginal(),
                archivo == null ? null : archivo.getTipoContenido(), archivo == null ? null : archivo.getTamano());
    }
}
