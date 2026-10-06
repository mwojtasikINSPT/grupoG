package prog2.policia_backend.services;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.JuezDTO;
import prog2.policia_backend.exceptions.FechaFuturaException;
import prog2.policia_backend.exceptions.JuezConCasosJudicialesException;
import prog2.policia_backend.exceptions.MotivoBajaObligatorioException;
import prog2.policia_backend.exceptions.PersonaInactivaException;
import prog2.policia_backend.exceptions.PersonaNoReactivableException;
import prog2.policia_backend.exceptions.PersonaYaActivaException;
import prog2.policia_backend.exceptions.PersonaYaInactivaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Juez;
import prog2.policia_backend.models.MotivoBajaPersona;
import prog2.policia_backend.repositories.JuezRepository;
import prog2.policia_backend.repositories.CasoJudicialRepository;
import prog2.policia_backend.utils.GeneradorCodigo;
import prog2.policia_backend.utils.NormalizadorTexto;

@Service
@RequiredArgsConstructor
public class JuezService {

    private final JuezRepository juezRepository;
    private final CasoJudicialRepository casoJudicialRepository;

    public List<JuezDTO> listar(Boolean activo) {

        List<Juez> jueces;

        if (activo == null) {
            jueces = juezRepository.findAll();
        } else {
            jueces = juezRepository.findByActivo(activo);
        }

        return jueces.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public JuezDTO buscarPorCodigo(String codigo) {
        return convertirADTO(obtenerJuez(codigo));
    }

    public List<JuezDTO> buscarPorNombre(String nombre, Boolean activo) {

        String nombreNormalizado = NormalizadorTexto.normalizarParaBuscar(nombre);

        return juezRepository.findAll()
                .stream()
                .filter(juez -> activo == null || juez.isActivo() == activo)
                .filter(juez -> NormalizadorTexto.normalizarParaBuscar(juez.getNombre())
                .contains(nombreNormalizado))
                .map(this::convertirADTO)
                .toList();
    }

    public JuezDTO guardar(JuezDTO dto) {
        Juez juez = convertirAEntidad(dto);

        juez = juezRepository.save(juez);

        juez.setCodigo(GeneradorCodigo.generar("JUE", juez.getId()));
        juez.setActivo(true);

        juez = juezRepository.save(juez);

        return convertirADTO(juez);
    }

    public JuezDTO actualizar(String codigo, JuezDTO dto) {

        Juez juez = obtenerJuez(codigo);

        if (!juez.isActivo()) {
            throw new PersonaInactivaException();
        }

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            juez.setNombre(NormalizadorTexto.normalizarParaGuardar(dto.getNombre()));
        }

        validarYAsignarFecha(dto, juez);

        return convertirADTO(juezRepository.save(juez));
    }

    public void eliminar(String codigo, JuezDTO dto) {

        Juez juez = obtenerJuez(codigo);

        if (dto.getMotivoBaja() == null) {
            throw new MotivoBajaObligatorioException();
        }

        if (!juez.isActivo()) {
            throw new PersonaYaInactivaException();
        }

        if (casoJudicialRepository.existsByJuez_Id(juez.getId())) {
            throw new JuezConCasosJudicialesException();
        }

        juez.setActivo(false);
        juez.setMotivoBaja(dto.getMotivoBaja());

        juezRepository.save(juez);
    }

    public JuezDTO reactivar(String codigo) {

        Juez juez = obtenerJuez(codigo);

        if (juez.getMotivoBaja() == MotivoBajaPersona.FALLECIMIENTO) {
            throw new PersonaNoReactivableException();
        }

        if (juez.isActivo()) {
            throw new PersonaYaActivaException();
        }

        juez.setActivo(true);
        juez.setMotivoBaja(null);
        juez = juezRepository.save(juez);

        return convertirADTO(juez);
    }

    //toma la fecha juezDesde y calcula automáticamente los años hasta hoy
    private JuezDTO convertirADTO(Juez juez) {
        int aniosServicio = juez.getJuezDesde() != null
                ? Period.between(juez.getJuezDesde(), LocalDate.now()).getYears()
                : 0;

        JuezDTO dto = new JuezDTO();

        dto.setId(juez.getId());
        dto.setNombre(juez.getNombre());
        dto.setJuezDesde(juez.getJuezDesde());
        dto.setAniosServicio(aniosServicio);
        dto.setCodigo(juez.getCodigo());
        dto.setActivo(juez.isActivo());
        dto.setMotivoBaja(juez.getMotivoBaja());
        dto.setFechaCreacion(juez.getFechaCreacion());
        dto.setFechaModificacion(juez.getFechaModificacion());
        dto.setCreadoPor(juez.getCreadoPor());
        dto.setModificadoPor(juez.getModificadoPor());

        return dto;
    }

    private Juez convertirAEntidad(JuezDTO dto) {
        Juez juez = new Juez();

        juez.setNombre(NormalizadorTexto.normalizarParaGuardar(dto.getNombre()));
        validarYAsignarFecha(dto, juez);

        return juez;
    }

//-------Aux--------
    private Juez obtenerJuez(String codigo) {
        return juezRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Juez", codigo));
    }

    private void validarYAsignarFecha(JuezDTO dto, Juez juez) {
        if (dto.getJuezDesde() != null) {
            if (dto.getJuezDesde().isAfter(LocalDate.now())) {
                throw new FechaFuturaException();
            }
            juez.setJuezDesde(dto.getJuezDesde());
        }
    }
}
