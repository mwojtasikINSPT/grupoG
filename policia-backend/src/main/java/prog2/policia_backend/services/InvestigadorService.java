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
import prog2.policia_backend.repositories.InvestigadorRepository;
import prog2.policia_backend.utils.GeneradorCodigo;
import prog2.policia_backend.models.RolUsuario;
import prog2.policia_backend.utils.NormalizadorTexto;

@Service
@RequiredArgsConstructor
public class InvestigadorService {

    private final InvestigadorRepository investigadorRepository;
    private final PasswordEncoder passwordEncoder;

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

        return investigadorRepository.findAll()
                .stream()
                .filter(investigador -> activo == null || investigador.isActivo() == activo)
                .filter(investigador -> NormalizadorTexto.normalizarParaBuscar(investigador.getNombre())
                .contains(nombreNormalizado))
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
        investigador.setRol(RolUsuario.INVESTIGADOR);
        investigador.setPassword(passwordEncoder.encode(dto.getPassword()));

        return investigador;
    }

    //-----Métodos Aux-------
    private Investigador obtenerInvestigador(String codigo) {
        return investigadorRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Investigador", codigo));
    }

}
