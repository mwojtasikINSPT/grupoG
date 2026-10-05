package prog2.policia_backend.services;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.VigilanteDTO;
import prog2.policia_backend.exceptions.EdadMaximaExcedidaException;
import prog2.policia_backend.exceptions.MotivoBajaObligatorioException;
import prog2.policia_backend.exceptions.PersonaInactivaException;
import prog2.policia_backend.exceptions.PersonaNoReactivableException;
import prog2.policia_backend.exceptions.PersonaYaActivaException;
import prog2.policia_backend.exceptions.PersonaYaInactivaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.exceptions.VigilanteConContratoFuturoException;
import prog2.policia_backend.models.MotivoBajaPersona;
import prog2.policia_backend.models.RolUsuario;
import prog2.policia_backend.models.Vigilante;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;
import prog2.policia_backend.repositories.VigilanteRepository;
import prog2.policia_backend.utils.GeneradorCodigo;
import prog2.policia_backend.utils.NormalizadorTexto;

@Service
@RequiredArgsConstructor //inyecto atrb final
public class VigilanteService {

    private final VigilanteRepository vigilanteRepository;
    private final PasswordEncoder passwordEncoder;
    private final ContratoVigilanciaRepository contratoVigilanciaRepository;

    public List<VigilanteDTO> listar(Boolean activo) {

        List<Vigilante> vigilantes;

        if (activo == null) {
            vigilantes = vigilanteRepository.findAll();
        } else {
            vigilantes = vigilanteRepository.findByActivo(activo);
        }

        return vigilantes.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public VigilanteDTO buscarPorCodigo(String codigo) {
        return convertirADTO(obtenerVigilante(codigo));
    }

    public List<VigilanteDTO> buscarPorNombre(String nombre, Boolean activo) {

        String nombreNormalizado = NormalizadorTexto.normalizarParaBuscar(nombre);

        return vigilanteRepository.findAll()
                .stream()
                .filter(vigilante -> activo == null || vigilante.isActivo() == activo)
                .filter(vigilante -> NormalizadorTexto.normalizarParaBuscar(vigilante.getNombre())
                .contains(nombreNormalizado))
                .map(this::convertirADTO)
                .toList();
    }

    public VigilanteDTO guardar(VigilanteDTO dto) {

        validarEdad(dto.getEdad());

        Vigilante vigilante = convertirAEntidad(dto);

        vigilante = vigilanteRepository.save(vigilante);

        vigilante.setCodigo(GeneradorCodigo.generar("VIG", vigilante.getId()));
        vigilante.setActivo(true);

        vigilante = vigilanteRepository.save(vigilante);

        return convertirADTO(vigilante);
    }

    public VigilanteDTO actualizar(String codigo, VigilanteDTO dto) {

        Vigilante vigilante = obtenerVigilante(codigo);

        if (!vigilante.isActivo()) {
            throw new PersonaInactivaException();
        }

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            vigilante.setNombre(NormalizadorTexto.normalizarParaGuardar(dto.getNombre()));
        }

        if (dto.getEdad() != null) {
            validarEdad(dto.getEdad()); 
            vigilante.setEdad(dto.getEdad());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            vigilante.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return convertirADTO(vigilanteRepository.save(vigilante));
    }

    public void eliminar(String codigo, VigilanteDTO dto) {

        Vigilante vigilante = obtenerVigilante(codigo);

        if (!vigilante.isActivo()) {
            throw new PersonaYaInactivaException();
        }

        if (dto.getMotivoBaja() == null) {
            throw new MotivoBajaObligatorioException();
        }

        if (contratoVigilanciaRepository
                .existsByVigilante_IdAndFechaGreaterThanEqualAndActivoTrue(vigilante.getId(), LocalDate.now())) {

            throw new VigilanteConContratoFuturoException();
        }

        vigilante.setActivo(false);
        vigilante.setMotivoBaja(dto.getMotivoBaja());

        vigilanteRepository.save(vigilante);
    }

    public VigilanteDTO reactivar(String codigo) {

        Vigilante vigilante = obtenerVigilante(codigo);

        if (vigilante.isActivo()) {
            throw new PersonaYaActivaException();
        }

        if (vigilante.getMotivoBaja() == MotivoBajaPersona.FALLECIMIENTO) {
            throw new PersonaNoReactivableException();
        }

        vigilante.setActivo(true);
        vigilante.setMotivoBaja(null);

        vigilante = vigilanteRepository.save(vigilante);

        return convertirADTO(vigilante);
    }

    private VigilanteDTO convertirADTO(Vigilante vigilante) {
        VigilanteDTO dto = new VigilanteDTO();

        dto.setId(vigilante.getId());
        dto.setCodigo(vigilante.getCodigo());
        dto.setNombre(vigilante.getNombre());
        dto.setPassword(null); // El password nunca devolvemos
        dto.setEdad(vigilante.getEdad());
        dto.setActivo(vigilante.isActivo());
        dto.setMotivoBaja(vigilante.getMotivoBaja());
        dto.setFechaCreacion(vigilante.getFechaCreacion());
        dto.setFechaModificacion(vigilante.getFechaModificacion());
        dto.setCreadoPor(vigilante.getCreadoPor());
        dto.setModificadoPor(vigilante.getModificadoPor());

        return dto;
    }

    private Vigilante convertirAEntidad(VigilanteDTO dto) {
        Vigilante vigilante = new Vigilante();

        vigilante.setCodigo(dto.getCodigo());
        vigilante.setNombre(
                NormalizadorTexto.normalizarParaGuardar(dto.getNombre())
        );
        vigilante.setEdad(dto.getEdad());
        vigilante.setRol(RolUsuario.VIGILANTE);
        vigilante.setPassword(
                passwordEncoder.encode(dto.getPassword())
        );

        return vigilante;
    }

//-------Aux---------
    private Vigilante obtenerVigilante(String codigo) {
        return vigilanteRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vigilante", codigo));
    }

    private void validarEdad(Integer edad) {
        if (edad != null && edad > 65) {
            throw new EdadMaximaExcedidaException();
        }
    }
}
