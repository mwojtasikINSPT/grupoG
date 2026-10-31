package prog2.policia_backend.services;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import prog2.policia_backend.DTOs.VigilanteDTO;
import prog2.policia_backend.exceptions.EdadInvalidaException;
import prog2.policia_backend.exceptions.MotivoBajaObligatorioException;
import prog2.policia_backend.exceptions.PersonaInactivaException;
import prog2.policia_backend.exceptions.PersonaNoReactivableException;
import prog2.policia_backend.exceptions.PersonaYaActivaException;
import prog2.policia_backend.exceptions.PersonaYaInactivaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.exceptions.VigilanteConContratoFuturoException;
import prog2.policia_backend.models.MotivoBajaPersona;
import prog2.policia_backend.models.Rol;
import prog2.policia_backend.models.Vigilante;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;
import prog2.policia_backend.repositories.RolRepository;
import prog2.policia_backend.repositories.VigilanteRepository;
import prog2.policia_backend.utils.GeneradorCodigo;
import prog2.policia_backend.utils.NormalizadorTexto;

@Service
@RequiredArgsConstructor // inyecto atrb final
public class VigilanteService {

    private final VigilanteRepository vigilanteRepository;
    private final PasswordEncoder passwordEncoder;
    private final ContratoVigilanciaRepository contratoVigilanciaRepository;
    private final RolRepository rolRepository;

    public List<VigilanteDTO> listar(Boolean activo) {

        List<Vigilante> vigilantes;

        if (activo == null) {
            vigilantes = vigilanteRepository.findAll();
        } else {
            vigilantes = vigilanteRepository.findByActivo(activo);
        }

        List<VigilanteDTO> resultado = new ArrayList<>();

        for (Vigilante vigilante : vigilantes) {
            resultado.add(convertirADTO(vigilante));
        }

        return resultado;
    }

    public VigilanteDTO buscarPorCodigo(String codigo) {
        return convertirADTO(obtenerVigilante(codigo));
    }

    public List<VigilanteDTO> buscarPorNombre(String nombre, Boolean activo) {
        String nombreNormalizado = NormalizadorTexto.normalizarParaBuscar(nombre);
        List<Vigilante> vigilantes;

        if (activo == null) {
            vigilantes = vigilanteRepository.findByNombreContainingIgnoreCase(nombreNormalizado);
        } else {
            vigilantes = vigilanteRepository.findByNombreContainingIgnoreCaseAndActivo(nombreNormalizado, activo);
        }

        List<VigilanteDTO> resultado = new ArrayList<>();

        for (Vigilante vigilante : vigilantes) {
            resultado.add(convertirADTO(vigilante));
        }

        return resultado;
    }

    @Transactional
    public VigilanteDTO guardar(VigilanteDTO dto) {
        Vigilante vigilante = convertirAEntidad(dto);
        vigilante.setActivo(true);

        vigilante = vigilanteRepository.save(vigilante);
        vigilante.setCodigo(generarCodigoDisponible());
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

        if (contratoVigilanciaRepository.existsByVigilante_IdAndFechaGreaterThanEqualAndActivoTrue(vigilante.getId(),
                LocalDate.now())) {

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
        dto.setRol(vigilante.getRol().getNombre());
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
        vigilante.setNombre(NormalizadorTexto.normalizarParaGuardar(dto.getNombre()));
        validarEdad(dto.getEdad());
        vigilante.setEdad(dto.getEdad());

        Optional<Rol> resultado = rolRepository.findByNombre("VIGILANTE");

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Rol", "VIGILANTE");
        }

        Rol rol = resultado.get();
        vigilante.setRol(rol);
        vigilante.setPassword(passwordEncoder.encode(dto.getPassword()));

        return vigilante;
    }

    public List<VigilanteDTO> listarPropio(String codigo, Boolean activo, String nombre) {
        Vigilante vigilante = obtenerVigilante(codigo);

        if (activo != null && vigilante.isActivo() != activo) {
            return List.of();
        }

        if (nombre != null && !nombre.isBlank()) {
            String nombreNormalizado = NormalizadorTexto.normalizarParaBuscar(nombre);
            String nombreVigilante = NormalizadorTexto.normalizarParaBuscar(vigilante.getNombre());

            if (!nombreVigilante.contains(nombreNormalizado)) {
                return List.of();
            }
        }

        return List.of(convertirADTO(vigilante));
    }

    // -------Aux---------
    private Vigilante obtenerVigilante(String codigo) {
        String codigoNormalizado = codigo != null ? codigo.trim().toUpperCase() : null;

        Optional<Vigilante> resultado = vigilanteRepository.findByCodigo(codigoNormalizado);

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Vigilante", codigoNormalizado);
        }

        return resultado.get();
    }

    private void validarEdad(Integer edad) {
        if (edad == null || edad < 18 || edad > 65) {
            throw new EdadInvalidaException();
        }
    }

    private String generarCodigoDisponible() {
        long numero = 1;
        String codigo;

        do {
            codigo = GeneradorCodigo.generar("VIG", numero);
            numero++;
        } while (vigilanteRepository.existsByCodigo(codigo));

        return codigo;
    }
}
