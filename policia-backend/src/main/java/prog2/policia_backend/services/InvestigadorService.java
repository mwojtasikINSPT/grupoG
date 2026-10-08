package prog2.policia_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;

import prog2.policia_backend.DTOs.InvestigadorDTO;
import prog2.policia_backend.exceptions.MotivoBajaObligatorioException;
import prog2.policia_backend.exceptions.PersonaInactivaException;
import prog2.policia_backend.exceptions.PersonaNoReactivableException;
import prog2.policia_backend.exceptions.PersonaYaActivaException;
import prog2.policia_backend.exceptions.PersonaYaInactivaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Investigador;
import prog2.policia_backend.models.MotivoBajaPersona;
import prog2.policia_backend.models.Rol;
import prog2.policia_backend.repositories.InvestigadorRepository;
import prog2.policia_backend.repositories.RolRepository;
import prog2.policia_backend.utils.GeneradorCodigo;
import prog2.policia_backend.utils.NormalizadorTexto;

@Service
@RequiredArgsConstructor
public class InvestigadorService {

    private final InvestigadorRepository investigadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;

    public List<InvestigadorDTO> listar(Boolean activo) {

        List<Investigador> investigadores;

        if (activo == null) {
            investigadores = investigadorRepository.findAll();
        } else {
            investigadores = investigadorRepository.findByActivo(activo);
        }

        return investigadores.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public InvestigadorDTO buscarPorCodigo(String codigo) {
        return convertirADTO(obtenerInvestigador(codigo));
    }

    public List<InvestigadorDTO> buscarPorNombre(String nombre, Boolean activo) {
        String nombreNormalizado = NormalizadorTexto.normalizarParaBuscar(nombre);
        List<Investigador> investigadores;

        if (activo == null) {
            investigadores = investigadorRepository.findByNombreContainingIgnoreCase(nombreNormalizado);
        } else {
            investigadores = investigadorRepository.findByNombreContainingIgnoreCaseAndActivo(nombreNormalizado, activo);
        }

        return investigadores.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public InvestigadorDTO guardar(InvestigadorDTO dto) {
        Investigador investigador = convertirAEntidad(dto);

        investigador = investigadorRepository.save(investigador);
        investigador.setCodigo(GeneradorCodigo.generar("INV", investigador.getId()));
        investigador.setActivo(true);
        investigador = investigadorRepository.save(investigador);

        return convertirADTO(investigador);
    }

    public InvestigadorDTO actualizar(String codigo, InvestigadorDTO dto) {

        Investigador investigador = obtenerInvestigador(codigo);

        if (!investigador.isActivo()) {
            throw new PersonaInactivaException();
        }

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            investigador.setNombre(NormalizadorTexto.normalizarParaGuardar(dto.getNombre()));
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            investigador.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return convertirADTO(investigadorRepository.save(investigador));
    }

    public void eliminar(String codigo, InvestigadorDTO dto) {

        Investigador investigador = obtenerInvestigador(codigo);

        if (!investigador.isActivo()) {
            throw new PersonaYaInactivaException();
        }

        if (dto.getMotivoBaja() == null) {
            throw new MotivoBajaObligatorioException();
        }

        investigador.setActivo(false);
        investigador.setMotivoBaja(dto.getMotivoBaja());

        investigadorRepository.save(investigador);
    }

    public InvestigadorDTO reactivar(String codigo) {

        Investigador investigador = obtenerInvestigador(codigo);

        if (investigador.isActivo()) {
            throw new PersonaYaActivaException();
        }

        if (investigador.getMotivoBaja() == MotivoBajaPersona.FALLECIMIENTO) {
            throw new PersonaNoReactivableException();
        }

        investigador.setActivo(true);
        investigador.setMotivoBaja(null);

        investigador = investigadorRepository.save(investigador);

        return convertirADTO(investigador);
    }

    private InvestigadorDTO convertirADTO(Investigador investigador) {
        InvestigadorDTO dto = new InvestigadorDTO();

        dto.setId(investigador.getId());
        dto.setCodigo(investigador.getCodigo());
        dto.setNombre(investigador.getNombre());
        dto.setPassword(null); // password
        dto.setActivo(investigador.isActivo());
        dto.setRol(investigador.getRol().getNombre());
        dto.setMotivoBaja(investigador.getMotivoBaja());
        dto.setFechaCreacion(investigador.getFechaCreacion());
        dto.setFechaModificacion(investigador.getFechaModificacion());
        dto.setCreadoPor(investigador.getCreadoPor());
        dto.setModificadoPor(investigador.getModificadoPor());

        return dto;
    }

    private Investigador convertirAEntidad(InvestigadorDTO dto) {
        Investigador investigador = new Investigador();

        investigador.setCodigo(dto.getCodigo());
        investigador.setNombre(NormalizadorTexto.normalizarParaGuardar(dto.getNombre()));
        Rol rol = rolRepository.findByNombre("INVESTIGADOR")
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol", "INVESTIGADOR"));
        investigador.setRol(rol);
        investigador.setPassword(passwordEncoder.encode(dto.getPassword()));

        return investigador;
    }

    //-----Métodos Aux-------
    private Investigador obtenerInvestigador(String codigo) {

        String codigoNormalizado = codigo != null ? codigo.trim().toUpperCase() : null;

        return investigadorRepository.findByCodigo(codigoNormalizado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Investigador", codigoNormalizado));
    }

}
