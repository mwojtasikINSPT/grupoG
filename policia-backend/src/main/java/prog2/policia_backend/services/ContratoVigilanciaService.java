package prog2.policia_backend.services;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.ContratoVigilanciaDTO;
import prog2.policia_backend.exceptions.ContratoVigilanciaCumplidoException;
import prog2.policia_backend.exceptions.ContratoVigilanciaDuplicadoException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.ContratoVigilancia;
import prog2.policia_backend.models.MotivoBajaContrato;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.models.Vigilante;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;
import prog2.policia_backend.repositories.SucursalRepository;
import prog2.policia_backend.repositories.VigilanteRepository;
import prog2.policia_backend.utils.GeneradorCodigo;

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
        return contratoVigilanciaRepository.findByCodigo(codigo)
                //.filter(ContratoVigilancia::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "ContratoVigilancia", codigo));
    }

    public ContratoVigilanciaDTO guardar(ContratoVigilanciaDTO dto) {

        if (contratoVigilanciaRepository
                .existsByVigilante_IdAndFechaAndActivoTrue(
                        dto.getVigilanteId(),
                        dto.getFecha())) {

            throw new ContratoVigilanciaDuplicadoException();
        }

        ContratoVigilancia contrato = convertirAEntidad(dto);

        contrato = contratoVigilanciaRepository.save(contrato);

        contrato.setCodigo(
                GeneradorCodigo.generar("CDV", contrato.getId())
        );

        contrato = contratoVigilanciaRepository.save(contrato);

        return convertirADTO(contrato);
    }

    public ContratoVigilanciaDTO actualizar(
            String codigo,
            ContratoVigilanciaDTO dto) {

        ContratoVigilancia contrato = contratoVigilanciaRepository
                .findByCodigo(codigo)
                .filter(ContratoVigilancia::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "ContratoVigilancia", codigo));

        boolean existeOtroContrato
                = contratoVigilanciaRepository
                        .existsByVigilante_IdAndFechaAndActivoTrueAndIdNot(
                                dto.getVigilanteId(),
                                dto.getFecha(),
                                contrato.getId());

        if (existeOtroContrato) {
            throw new ContratoVigilanciaDuplicadoException();
        }

        Vigilante vigilante = vigilanteRepository.findById(
                dto.getVigilanteId())
                .filter(Vigilante::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "Vigilante", dto.getVigilanteId()));

        Sucursal sucursal = sucursalRepository.findById(
                dto.getSucursalId())
                .filter(Sucursal::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "Sucursal", dto.getSucursalId()));

        contrato.setFecha(dto.getFecha());
        contrato.setConArma(dto.isConArma());
        contrato.setVigilante(vigilante);
        contrato.setSucursal(sucursal);

        return convertirADTO(
                contratoVigilanciaRepository.save(contrato)
        );
    }

    public void eliminar(String codigo) {

        ContratoVigilancia contrato = contratoVigilanciaRepository
                .findByCodigo(codigo)
                .filter(ContratoVigilancia::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "ContratoVigilancia", codigo));

        if (!contrato.getFecha().isAfter(LocalDate.now())) {
            throw new ContratoVigilanciaCumplidoException();
        }

        contrato.setActivo(false);
        contrato.setMotivoBaja(MotivoBajaContrato.CANCELACION);

        contratoVigilanciaRepository.save(contrato);
    }

    public List<ContratoVigilanciaDTO> listarPorVigilante(String codigo) {

        Vigilante vigilante = vigilanteRepository.findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "Vigilante", codigo));

        return contratoVigilanciaRepository
                .findByVigilante_Id(vigilante.getId())
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    private ContratoVigilanciaDTO convertirADTO(
            ContratoVigilancia contrato) {

        return new ContratoVigilanciaDTO(
                contrato.getId(),
                contrato.getFecha(),
                contrato.isConArma(),
                contrato.getVigilante().getId(),
                contrato.getSucursal().getId(),
                contrato.getCodigo(),
                contrato.isActivo(),
                contrato.getMotivoBaja(),
                contrato.getFechaCreacion(),
                contrato.getFechaModificacion(),
                contrato.getCreadoPor(),
                contrato.getModificadoPor()
        );
    }

    private ContratoVigilancia convertirAEntidad(
            ContratoVigilanciaDTO dto) {

        ContratoVigilancia contrato = new ContratoVigilancia();

        contrato.setFecha(dto.getFecha());
        contrato.setConArma(dto.isConArma());

        contrato.setVigilante(
                vigilanteRepository.findById(dto.getVigilanteId())
                        .filter(Vigilante::isActivo)
                        .orElseThrow(()
                                -> new RecursoNoEncontradoException(
                                "Vigilante",
                                dto.getVigilanteId()))
        );

        contrato.setSucursal(
                sucursalRepository.findById(dto.getSucursalId())
                        .filter(Sucursal::isActivo)
                        .orElseThrow(()
                                -> new RecursoNoEncontradoException(
                                "Sucursal",
                                dto.getSucursalId()))
        );

        return contrato;
    }
}
