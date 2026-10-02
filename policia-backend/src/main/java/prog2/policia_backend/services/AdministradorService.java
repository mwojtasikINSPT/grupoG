package prog2.policia_backend.services;

import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.AdministradorDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Administrador;
import prog2.policia_backend.repositories.AdministradorRepository;
import prog2.policia_backend.models.RolUsuario;
import prog2.policia_backend.utils.GeneradorCodigo;


@Service
@RequiredArgsConstructor
public class AdministradorService {

    private final AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<AdministradorDTO> listar() {
        return administradorRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public AdministradorDTO buscarPorId(Long id) {
        return administradorRepository.findById(id)
                .filter(Administrador::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador", id));
    }

    public AdministradorDTO guardar(AdministradorDTO dto) {
        Administrador administrador = convertirAEntidad(dto);

        administrador = administradorRepository.save(administrador);

        administrador.setCodigo(
                GeneradorCodigo.generar("ADM", administrador.getId())
        );

        return convertirADTO(administradorRepository.save(administrador));
    }

    public AdministradorDTO actualizar(Long id, AdministradorDTO dto) {
        Administrador administrador = administradorRepository.findById(id)
                .filter(Administrador::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador", id));

        administrador.setNombre(dto.getNombre());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            administrador.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return convertirADTO(administradorRepository.save(administrador));
    }

    public void eliminar(Long id) {
        Administrador administrador = administradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador", id));

        administrador.setActivo(false);
        administradorRepository.save(administrador);
    }

    private AdministradorDTO convertirADTO(Administrador administrador) {
        return new AdministradorDTO(
                administrador.getId(),
                administrador.getCodigo(),
                administrador.getNombre(),
                null
        );
    }

    private Administrador convertirAEntidad(AdministradorDTO dto) {
        Administrador administrador = new Administrador();

        administrador.setCodigo(dto.getCodigo());
        administrador.setNombre(dto.getNombre());
        administrador.setPassword(passwordEncoder.encode(dto.getPassword()));
        administrador.setRol(RolUsuario.ADMINISTRADOR);

        return administrador;
    }
}
