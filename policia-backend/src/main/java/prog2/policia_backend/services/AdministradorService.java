package prog2.policia_backend.services;

import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.AdministradorDTO;
import prog2.policia_backend.exceptions.PersonaNoReactivableException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Administrador;
import prog2.policia_backend.models.MotivoBajaPersona;
import prog2.policia_backend.repositories.AdministradorRepository;
import prog2.policia_backend.models.RolUsuario;
import prog2.policia_backend.utils.GeneradorCodigo;

@Service
@RequiredArgsConstructor
public class AdministradorService {

    private final AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<AdministradorDTO> listar(Boolean activo) {

        List<Administrador> administradores;

        if (activo == null) {
            administradores = administradorRepository.findAll();
        } else {
            administradores = administradorRepository.findByActivo(activo);
        }

        return administradores.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public AdministradorDTO buscarPorCodigo(String codigo) {
        return administradorRepository.findByCodigo(codigo)
                .filter(Administrador::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Administrador", codigo));
    }

    public AdministradorDTO guardar(AdministradorDTO dto) {
        Administrador administrador = convertirAEntidad(dto);

        administrador = administradorRepository.save(administrador);

        administrador.setCodigo(
                GeneradorCodigo.generar("ADM", administrador.getId())
        );

        administrador = administradorRepository.save(administrador);

        return convertirADTO(administrador);
    }

    public AdministradorDTO actualizar(String codigo, AdministradorDTO dto) {

        Administrador administrador = administradorRepository.findByCodigo(codigo)
                .filter(Administrador::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Administrador", codigo));

        administrador.setNombre(dto.getNombre());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            administrador.setPassword(
                    passwordEncoder.encode(dto.getPassword())
            );
        }

        return convertirADTO(administradorRepository.save(administrador));
    }

    public void eliminar(String codigo, AdministradorDTO dto) {

        Administrador administrador = administradorRepository.findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Administrador", codigo));

        if (administrador.getMotivoBaja() == MotivoBajaPersona.FALLECIMIENTO) {
            throw new PersonaNoReactivableException();
        }

        administrador.setActivo(false);
        administrador.setMotivoBaja(dto.getMotivoBaja());

        administradorRepository.save(administrador);
    }

    public AdministradorDTO reactivar(String codigo) {

        Administrador administrador = administradorRepository.findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Administrador", codigo));

        if (administrador.getMotivoBaja() == MotivoBajaPersona.FALLECIMIENTO) {
            throw new PersonaNoReactivableException();
        }

        administrador.setActivo(true);
        administrador.setMotivoBaja(null);

        administrador = administradorRepository.save(administrador);

        return convertirADTO(administrador);
    }

    private AdministradorDTO convertirADTO(Administrador administrador) {
        return new AdministradorDTO(
                administrador.getId(),
                administrador.getCodigo(),
                administrador.getNombre(),
                null,
                administrador.getMotivoBaja(),
                administrador.getFechaCreacion(),
                administrador.getFechaModificacion(),
                administrador.getCreadoPor(),
                administrador.getModificadoPor()
        );
    }

    private Administrador convertirAEntidad(AdministradorDTO dto) {
        Administrador administrador = new Administrador();

        administrador.setCodigo(dto.getCodigo());
        administrador.setNombre(dto.getNombre());
        administrador.setPassword(
                passwordEncoder.encode(dto.getPassword())
        );
        administrador.setRol(RolUsuario.ADMINISTRADOR);

        return administrador;
    }
}
