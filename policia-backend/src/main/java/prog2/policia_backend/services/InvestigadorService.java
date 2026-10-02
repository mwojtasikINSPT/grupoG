package prog2.policia_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;

import prog2.policia_backend.DTOs.InvestigadorDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Investigador;
import prog2.policia_backend.repositories.InvestigadorRepository;
import prog2.policia_backend.utils.GeneradorCodigo;
import prog2.policia_backend.models.RolUsuario;

@Service
@RequiredArgsConstructor
public class InvestigadorService {

    private final InvestigadorRepository investigadorRepository;
    private final PasswordEncoder passwordEncoder;

    public List<InvestigadorDTO> listar() {
        return investigadorRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public InvestigadorDTO buscarPorId(Long id) {
        return investigadorRepository.findById(id)
                .filter(Investigador::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Investigador", id));
    }

    public InvestigadorDTO guardar(InvestigadorDTO dto) {
        Investigador investigador = convertirAEntidad(dto);

        investigador = investigadorRepository.save(investigador);

        investigador.setCodigo(
                GeneradorCodigo.generar("INV", investigador.getId())
        );

        return convertirADTO(investigadorRepository.save(investigador));
    }

    public InvestigadorDTO actualizar(Long id, InvestigadorDTO dto) {
        Investigador investigador = investigadorRepository.findById(id)
                .filter(Investigador::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Investigador", id));

        investigador.setNombre(dto.getNombre());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            investigador.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return convertirADTO(investigadorRepository.save(investigador));
    }

    public void eliminar(Long id) {
        Investigador investigador = investigadorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Investigador", id));

        investigador.setActivo(false);
        investigadorRepository.save(investigador);
    }

    private InvestigadorDTO convertirADTO(Investigador investigador) {
        return new InvestigadorDTO(
                investigador.getId(),
                investigador.getCodigo(),
                investigador.getNombre(),
                null
        );
    }

    private Investigador convertirAEntidad(InvestigadorDTO dto) {
        Investigador investigador = new Investigador();

        investigador.setCodigo(dto.getCodigo());
        investigador.setNombre(dto.getNombre());
        investigador.setRol(RolUsuario.INVESTIGADOR);

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            investigador.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            throw new IllegalArgumentException("La contraseña es obligatoria para registrar un nuevo usuario");
        }

        return investigador;
    }
}
