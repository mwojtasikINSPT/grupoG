package prog2.policia_backend.services;

import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.AdministradorDTO;
import prog2.policia_backend.exceptions.MotivoBajaObligatorioException;
import prog2.policia_backend.exceptions.PersonaInactivaException;
import prog2.policia_backend.exceptions.PersonaNoReactivableException;
import prog2.policia_backend.exceptions.PersonaYaActivaException;
import prog2.policia_backend.exceptions.PersonaYaInactivaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Administrador;
import prog2.policia_backend.models.MotivoBajaPersona;
import prog2.policia_backend.repositories.AdministradorRepository;
import prog2.policia_backend.models.RolUsuario;
import prog2.policia_backend.utils.GeneradorCodigo;
import prog2.policia_backend.utils.NormalizadorTexto;

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
        return convertirADTO(obtenerAdministrador(codigo));
    }

    public List<AdministradorDTO> buscarPorNombre(String nombre, Boolean activo) {
        String nombreNormalizado
                = NormalizadorTexto.normalizarParaBuscar(nombre);

        return administradorRepository.findAll()
                .stream()
                .filter(administrador -> activo == null || administrador.isActivo() == activo)
                .filter(administrador -> NormalizadorTexto.normalizarParaBuscar(administrador.getNombre())
                .contains(nombreNormalizado))
                .map(this::convertirADTO)
                .toList();
    }

    public AdministradorDTO guardar(AdministradorDTO dto) {
        Administrador administrador = convertirAEntidad(dto);

        administrador = administradorRepository.save(administrador);

        administrador.setCodigo(GeneradorCodigo.generar("ADM", administrador.getId()));
        administrador.setActivo(true);

        administrador = administradorRepository.save(administrador);

        return convertirADTO(administrador);
    }

    public AdministradorDTO actualizar(String codigo, AdministradorDTO dto) {

        Administrador administrador = obtenerAdministrador(codigo);

        if (!administrador.isActivo()) {
            throw new PersonaInactivaException();
        }

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            administrador.setNombre(
                    NormalizadorTexto.normalizarParaGuardar(dto.getNombre()));
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            administrador.setPassword(
                    passwordEncoder.encode(dto.getPassword()));
        }

        return convertirADTO(administradorRepository.save(administrador));
    }

    public void eliminar(String codigo, AdministradorDTO dto) {

        Administrador administrador = obtenerAdministrador(codigo);

        if (!administrador.isActivo()) {
            throw new PersonaYaInactivaException();
        }

        if (dto.getMotivoBaja() == null) {
            throw new MotivoBajaObligatorioException();
        }

        administrador.setActivo(false);
        administrador.setMotivoBaja(dto.getMotivoBaja());

        administradorRepository.save(administrador);
    }

    public AdministradorDTO reactivar(String codigo) {

        Administrador administrador = obtenerAdministrador(codigo);

        if (administrador.isActivo()) {
            throw new PersonaYaActivaException();
        }

        if (administrador.getMotivoBaja() == MotivoBajaPersona.FALLECIMIENTO) {
            throw new PersonaNoReactivableException();
        }

        administrador.setActivo(true);
        administrador.setMotivoBaja(null);

        administrador = administradorRepository.save(administrador);

        return convertirADTO(administrador);
    }

    private AdministradorDTO convertirADTO(Administrador administrador) {
        AdministradorDTO dto = new AdministradorDTO();

        dto.setId(administrador.getId());
        dto.setCodigo(administrador.getCodigo());
        dto.setNombre(administrador.getNombre());
        // password no seteamos
        dto.setMotivoBaja(administrador.getMotivoBaja());
        dto.setActivo(administrador.isActivo());
        dto.setFechaCreacion(administrador.getFechaCreacion());
        dto.setFechaModificacion(administrador.getFechaModificacion());
        dto.setCreadoPor(administrador.getCreadoPor());
        dto.setModificadoPor(administrador.getModificadoPor());

        return dto;
    }

    private Administrador convertirAEntidad(AdministradorDTO dto) {
        Administrador administrador = new Administrador();

        administrador.setCodigo(dto.getCodigo());
        administrador.setNombre(NormalizadorTexto.normalizarParaGuardar(dto.getNombre()));
        administrador.setPassword(passwordEncoder.encode(dto.getPassword()));
        administrador.setRol(RolUsuario.ADMINISTRADOR);
        administrador.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        return administrador;
    }

    //------Métodos Aux----
    private Administrador obtenerAdministrador(String codigo) {

        return administradorRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador", codigo));

    }

}
