package prog2.policia_backend.services;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.ContratoVigilanciaDTO;
import prog2.policia_backend.exceptions.ContratoVigilanciaCumplidoException;
import prog2.policia_backend.exceptions.ContratoVigilanciaDuplicadoException;
import prog2.policia_backend.exceptions.PersonaInactivaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.exceptions.SucursalYaCerradaException;
import prog2.policia_backend.models.ContratoVigilancia;
import prog2.policia_backend.models.MotivoBajaContrato;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.models.Vigilante;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;
import prog2.policia_backend.repositories.SucursalRepository;
import prog2.policia_backend.repositories.VigilanteRepository;
import prog2.policia_backend.utils.GeneradorCodigo;
import prog2.policia_backend.utils.NormalizadorTexto;

@Service
@RequiredArgsConstructor
public class ContratoVigilanciaService {

    private final ContratoVigilanciaRepository contratoVigilanciaRepository;
    private final VigilanteRepository vigilanteRepository;
    private final SucursalRepository sucursalRepository;

    public List<ContratoVigilanciaDTO> listar(Boolean activo) {

        List<ContratoVigilancia> contratos;

        if (activo == null) {
            contratos = contratoVigilanciaRepository.findAll();
        } else {
            contratos = contratoVigilanciaRepository.findByActivo(activo);
        }

        return contratos.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public ContratoVigilanciaDTO buscarPorCodigo(String codigo) {
        return convertirADTO(obtenerContrato(codigo));
    }

    public List<ContratoVigilanciaDTO> buscarPorNombreVigilante(String nombre, Boolean activo) {

        String nombreNormalizado = NormalizadorTexto.normalizarParaBuscar(nombre);

        return contratoVigilanciaRepository.findAll()
                .stream()
                .filter(contrato -> activo == null || contrato.getActivo() == activo)
                .filter(contrato -> NormalizadorTexto.normalizarParaBuscar(contrato.getVigilante().getNombre())
                .contains(nombreNormalizado))
                .map(this::convertirADTO)
                .toList();
    }

    public ContratoVigilanciaDTO guardar(ContratoVigilanciaDTO dto) {

        Vigilante vigilante = obtenerVigilanteActivo(dto.getVigilanteCodigo());
        Sucursal sucursal = obtenerSucursalActiva(dto.getSucursalCodigo());

        if (contratoVigilanciaRepository.existsByVigilante_IdAndFechaAndActivoTrue(
                vigilante.getId(), dto.getFecha())) {
            throw new ContratoVigilanciaDuplicadoException();
        }

        ContratoVigilancia contrato = convertirAEntidad(dto, vigilante, sucursal);
        contrato = contratoVigilanciaRepository.save(contrato);
        contrato.setCodigo(GeneradorCodigo.generar("CDV", contrato.getId()));
        contrato.setActivo(true);

        contrato = contratoVigilanciaRepository.save(contrato);

        return convertirADTO(contrato);

    }

    public ContratoVigilanciaDTO actualizar(String codigo, ContratoVigilanciaDTO dto) {

        ContratoVigilancia contrato = obtenerContrato(codigo);

        if (!contrato.getActivo()) {
            throw new ContratoVigilanciaCumplidoException();
        }

        LocalDate fecha = dto.getFecha() != null ? dto.getFecha() : contrato.getFecha();

        Vigilante vigilante = contrato.getVigilante();
        if (dto.getVigilanteCodigo() != null && !dto.getVigilanteCodigo().isBlank()) {
            vigilante = obtenerVigilanteActivo(dto.getVigilanteCodigo());
        }

        boolean existeOtroContrato = contratoVigilanciaRepository
                .existsByVigilante_IdAndFechaAndActivoTrueAndIdNot(
                        vigilante.getId(),
                        fecha,
                        contrato.getId());

        if (existeOtroContrato) {
            throw new ContratoVigilanciaDuplicadoException();
        }

        if (dto.getFecha() != null) {
            contrato.setFecha(dto.getFecha());
        }

        if (dto.getConArma() != null) {
            contrato.setConArma(dto.getConArma());
        }
        contrato.setVigilante(vigilante);

        if (dto.getSucursalCodigo() != null && !dto.getSucursalCodigo().isBlank()) {
            Sucursal sucursal = obtenerSucursalActiva(dto.getSucursalCodigo());
            contrato.setSucursal(sucursal);
        }

        return convertirADTO(contratoVigilanciaRepository.save(contrato));
    }

    public void eliminar(String codigo) {

        ContratoVigilancia contrato = obtenerContrato(codigo);

        if (contrato.getFecha().isBefore(LocalDate.now())) {
            throw new ContratoVigilanciaCumplidoException();
        }

        contrato.setActivo(false);
        contrato.setMotivoBaja(MotivoBajaContrato.CANCELACION);

        contratoVigilanciaRepository.save(contrato);
    }

    public List<ContratoVigilanciaDTO> listarPorVigilante(String codigo) {

        Vigilante vigilante = obtenerVigilante(codigo);

        return contratoVigilanciaRepository
                .findByVigilante_Id(vigilante.getId())
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<ContratoVigilanciaDTO> listarPorSucursal(String codigo) {

        Sucursal sucursal = sucursalRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", codigo));

        return contratoVigilanciaRepository
                .findBySucursal_Id(sucursal.getId())
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    private ContratoVigilanciaDTO convertirADTO(
            ContratoVigilancia contrato) {

        return new ContratoVigilanciaDTO(
                contrato.getId(),
                contrato.getFecha(),
                contrato.getConArma(),
                contrato.getVigilante().getCodigo(),
                contrato.getSucursal().getCodigo(),
                contrato.getCodigo(),
                contrato.getActivo(),
                contrato.getMotivoBaja(),
                contrato.getFechaCreacion(),
                contrato.getFechaModificacion(),
                contrato.getCreadoPor(),
                contrato.getModificadoPor()
        );
    }

    private ContratoVigilancia convertirAEntidad(ContratoVigilanciaDTO dto, Vigilante vigilante, Sucursal sucursal) {

        ContratoVigilancia contrato = new ContratoVigilancia();
        contrato.setFecha(dto.getFecha());
        contrato.setConArma(dto.getConArma());
        contrato.setVigilante(vigilante);
        contrato.setSucursal(sucursal);

        return contrato;
    }

    //------Metodos Aux----
    private ContratoVigilancia obtenerContrato(String codigo) {
        return contratoVigilanciaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("ContratoVigilancia", codigo));
    }

    private Vigilante obtenerVigilante(String codigo) {
        return vigilanteRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vigilante", codigo));
    }

    private Vigilante obtenerVigilanteActivo(String codigo) {
        Vigilante vigilante = obtenerVigilante(codigo);

        if (!vigilante.isActivo()) {
            throw new PersonaInactivaException();
        }
        return vigilante;
    }

    private Sucursal obtenerSucursalActiva(String codigo) {
        Sucursal sucursal = sucursalRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", codigo));

        if (!sucursal.isActivo()) {
            throw new SucursalYaCerradaException();
        }
        return sucursal;
    }
}
