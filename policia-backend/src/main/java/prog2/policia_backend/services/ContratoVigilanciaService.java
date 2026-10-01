package prog2.policia_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import prog2.policia_backend.DTOs.ContratoVigilanciaDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.ContratoVigilancia;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;
import prog2.policia_backend.repositories.SucursalRepository;
import prog2.policia_backend.repositories.VigilanteRepository;

import java.util.List;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.models.Vigilante;

@Service
@RequiredArgsConstructor
public class ContratoVigilanciaService {

    private final ContratoVigilanciaRepository contratoVigilanciaRepository;
    private final VigilanteRepository vigilanteRepository;
    private final SucursalRepository sucursalRepository;

    public List<ContratoVigilanciaDTO> listar() {
        return contratoVigilanciaRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public ContratoVigilanciaDTO buscarPorId(Long id) {
        return contratoVigilanciaRepository.findById(id)
                .filter(ContratoVigilancia::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("ContratoVigilancia", id));
    }

    public ContratoVigilanciaDTO guardar(ContratoVigilanciaDTO dto) {
        ContratoVigilancia contrato = convertirAEntidad(dto);
        return convertirADTO(contratoVigilanciaRepository.save(contrato));
    }

    public ContratoVigilanciaDTO actualizar(Long id, ContratoVigilanciaDTO dto) {
        ContratoVigilancia contrato = contratoVigilanciaRepository.findById(id)
                .filter(ContratoVigilancia::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("ContratoVigilancia", id));

        contrato.setFecha(dto.getFecha());
        contrato.setConArma(dto.isConArma());

        Vigilante vigilante = vigilanteRepository.findById(dto.getVigilanteId())
                .filter(Vigilante::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Vigilante", dto.getVigilanteId()));

        Sucursal sucursal = sucursalRepository.findById(dto.getSucursalId())
                .filter(Sucursal::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Sucursal", dto.getSucursalId()));

        contrato.setVigilante(vigilante);
        contrato.setSucursal(sucursal);

        return convertirADTO(contratoVigilanciaRepository.save(contrato));
    }

    public void eliminar(Long id) {
        ContratoVigilancia contrato = contratoVigilanciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("ContratoVigilancia", id));

        contrato.setActivo(false);
        contratoVigilanciaRepository.save(contrato);
    }

    private ContratoVigilanciaDTO convertirADTO(ContratoVigilancia contrato) {
        return new ContratoVigilanciaDTO(
                contrato.getId(),
                contrato.getFecha(),
                contrato.isConArma(),
                contrato.getVigilante().getId(),
                contrato.getSucursal().getId(),
                contrato.getCodigo()
        );
    }

    private ContratoVigilancia convertirAEntidad(ContratoVigilanciaDTO dto) {
        ContratoVigilancia contrato = new ContratoVigilancia();

        contrato.setFecha(dto.getFecha());
        contrato.setConArma(dto.isConArma());

        contrato.setVigilante(
                vigilanteRepository.findById(dto.getVigilanteId())
                        .filter(Vigilante::isActivo)
                        .orElseThrow(()
                                -> new RecursoNoEncontradoException(
                                "Vigilante", dto.getVigilanteId()))
        );

        contrato.setSucursal(
                sucursalRepository.findById(dto.getSucursalId())
                        .filter(Sucursal::isActivo)
                        .orElseThrow(()
                                -> new RecursoNoEncontradoException(
                                "Sucursal", dto.getSucursalId()))
        );

        return contrato;
    }
}
