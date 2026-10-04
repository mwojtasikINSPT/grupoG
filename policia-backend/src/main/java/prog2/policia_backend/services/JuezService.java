package prog2.policia_backend.services;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.JuezDTO;
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
        return juezRepository.findByCodigo(codigo)
                //.filter(Juez::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Juez", codigo));
    }

    public JuezDTO reactivar(String codigo) {

        Juez juez = juezRepository.findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Juez", codigo));

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

    public JuezDTO guardar(JuezDTO dto) {
        Juez juez = convertirAEntidad(dto);

        juez = juezRepository.save(juez);

        juez.setCodigo(
                GeneradorCodigo.generar("JUE", juez.getId())
        );

        juez = juezRepository.save(juez);

        return convertirADTO(juez);
    }

    public JuezDTO actualizar(String codigo, JuezDTO dto) {

        Juez juez = juezRepository.findByCodigo(codigo)
                //.filter(Juez::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Juez", codigo));

        if (!juez.isActivo()) {
            throw new PersonaInactivaException();
        }

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            juez.setNombre(
                    NormalizadorTexto.normalizarParaGuardar(dto.getNombre())
            );
        }

        if (dto.getJuezDesde() != null) {
            juez.setJuezDesde(dto.getJuezDesde());
        }

        return convertirADTO(juezRepository.save(juez));
    }

    public void eliminar(String codigo, JuezDTO dto) {

        Juez juez = juezRepository.findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Juez", codigo));

        if (dto.getMotivoBaja() == null) {
            throw new MotivoBajaObligatorioException();
        }

        if (!juez.isActivo()) {
            throw new PersonaYaInactivaException();
        }

        if (juez.getMotivoBaja() == MotivoBajaPersona.FALLECIMIENTO) {
            throw new PersonaNoReactivableException();
        }

        if (casoJudicialRepository.existsByJuez_Id(juez.getId())) {
            throw new JuezConCasosJudicialesException();
        }

        juez.setActivo(false);
        juez.setMotivoBaja(dto.getMotivoBaja());

        juezRepository.save(juez);
    }

    //toma la fecha juezDesde y calcula automáticamente los años hasta hoy
    private JuezDTO convertirADTO(Juez juez) {
        int aniosServicio = Period.between(
                juez.getJuezDesde(),
                LocalDate.now()
        ).getYears();

        return new JuezDTO(
                juez.getId(),
                juez.getNombre(),
                juez.getJuezDesde(),
                aniosServicio,
                juez.getCodigo(),
                juez.getMotivoBaja(),
                juez.getFechaCreacion(),
                juez.getFechaModificacion(),
                juez.getCreadoPor(),
                juez.getModificadoPor()
        );
    }

    private Juez convertirAEntidad(JuezDTO dto) {
        Juez juez = new Juez();

        juez.setNombre(
                NormalizadorTexto.normalizarParaGuardar(dto.getNombre())
        );
        juez.setJuezDesde(dto.getJuezDesde());

        return juez;
    }
}
