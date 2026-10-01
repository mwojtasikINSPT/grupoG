package prog2.policia_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import prog2.policia_backend.DTOs.JuezDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Juez;
import prog2.policia_backend.repositories.JuezRepository;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JuezService {

    private final JuezRepository juezRepository;

    public List<JuezDTO> listar() {
        return juezRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public JuezDTO buscarPorId(Long id) {
        return juezRepository.findById(id)
                .filter(Juez::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Juez", id));
    }

    public JuezDTO guardar(JuezDTO dto) {
        Juez juez = convertirAEntidad(dto);
        return convertirADTO(juezRepository.save(juez));
    }

    public JuezDTO actualizar(Long id, JuezDTO dto) {
        Juez juez = juezRepository.findById(id)
                .filter(Juez::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Juez", id));

        juez.setNombre(dto.getNombre());
        juez.setJuezDesde(dto.getJuezDesde());

        return convertirADTO(juezRepository.save(juez));
    }

    public void eliminar(Long id) {
        Juez juez = juezRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Juez", id));

        juez.setActivo(false);
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
                juez.getCodigo()
        );
    }

    private Juez convertirAEntidad(JuezDTO dto) {
        Juez juez = new Juez();

        juez.setNombre(dto.getNombre());
        juez.setJuezDesde(dto.getJuezDesde());

        return juez;
    }
}
