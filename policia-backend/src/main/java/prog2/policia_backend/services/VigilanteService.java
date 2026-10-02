package prog2.policia_backend.services;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.VigilanteDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.exceptions.VigilanteConContratoFuturoException;
import prog2.policia_backend.models.RolUsuario;
import prog2.policia_backend.models.Vigilante;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;
import prog2.policia_backend.repositories.VigilanteRepository;
import prog2.policia_backend.utils.GeneradorCodigo;

@Service
@RequiredArgsConstructor //inyecto atrb final
public class VigilanteService {

    private final VigilanteRepository vigilanteRepository;
    private final PasswordEncoder passwordEncoder;
    private final ContratoVigilanciaRepository contratoVigilanciaRepository;

    public List<VigilanteDTO> listar() {
        return vigilanteRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public VigilanteDTO buscarPorId(Long id) {
        return vigilanteRepository.findById(id)
                .filter(Vigilante::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vigilante", id));
    }

    public VigilanteDTO guardar(VigilanteDTO dto) {
        Vigilante vigilante = convertirAEntidad(dto);

        vigilante = vigilanteRepository.save(vigilante);

        vigilante.setCodigo(
                GeneradorCodigo.generar("VIG", vigilante.getId())
        );

        return convertirADTO(vigilanteRepository.save(vigilante));
    }

    public VigilanteDTO actualizar(Long id, VigilanteDTO dto) {
        Vigilante vigilante = vigilanteRepository.findById(id)
                .filter(Vigilante::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vigilante", id));

        vigilante.setNombre(dto.getNombre());
        vigilante.setEdad(dto.getEdad());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            vigilante.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return convertirADTO(vigilanteRepository.save(vigilante));
    }

    public void eliminar(Long id) {
        Vigilante vigilante = vigilanteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vigilante", id));

        if (contratoVigilanciaRepository
                .existsByVigilante_IdAndFechaGreaterThanAndActivoTrue(
                        id, LocalDate.now())) {

            throw new VigilanteConContratoFuturoException();
        }

        vigilante.setActivo(false);
        vigilanteRepository.save(vigilante);
    }

    private VigilanteDTO convertirADTO(Vigilante vigilante) {
        return new VigilanteDTO(
                vigilante.getId(),
                vigilante.getCodigo(),
                vigilante.getNombre(),
                null,
                vigilante.getEdad()
        );
    }

    private Vigilante convertirAEntidad(VigilanteDTO dto) {
        Vigilante vigilante = new Vigilante();

        vigilante.setCodigo(dto.getCodigo());
        vigilante.setNombre(dto.getNombre());
        vigilante.setEdad(dto.getEdad());
        vigilante.setRol(RolUsuario.VIGILANTE);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            vigilante.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            throw new IllegalArgumentException("La contraseña es obligatoria para registrar un nuevo usuario");
        }

        return vigilante;
    }
}
