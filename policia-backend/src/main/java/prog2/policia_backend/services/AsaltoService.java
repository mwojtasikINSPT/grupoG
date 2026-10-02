package prog2.policia_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import prog2.policia_backend.DTOs.AsaltoDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Asalto;
import prog2.policia_backend.repositories.AsaltoRepository;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.SucursalRepository;

import java.util.List;
import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.utils.GeneradorCodigo;

@Service
@RequiredArgsConstructor
public class AsaltoService {

    private final AsaltoRepository asaltoRepository;
    private final AsaltanteRepository asaltanteRepository;
    private final SucursalRepository sucursalRepository;

    public List<AsaltoDTO> listar() {
        return asaltoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    /*
    public AsaltoDTO buscarPorId(Long id) {
        return asaltoRepository.findById(id)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asalto", id));
    }
     */
    public AsaltoDTO buscarPorCodigo(String codigo) {
        return asaltoRepository.findByCodigo(codigo)
                .map(this::convertirADTO)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Asalto", codigo));
    }

    public AsaltoDTO guardar(AsaltoDTO dto) {
        Asalto asalto = convertirAEntidad(dto);

        asalto = asaltoRepository.save(asalto);

        asalto.setCodigo(
                GeneradorCodigo.generar("AST", asalto.getId())
        );

        return convertirADTO(asaltoRepository.save(asalto));
    }

    public AsaltoDTO actualizar(String codigo, AsaltoDTO dto) {
        Asalto asalto = asaltoRepository.findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Asalto", codigo));

        asalto.setFecha(dto.getFecha());

        List<Asaltante> asaltantes = dto.getAsaltantesIds().stream()
                .map(idAsaltante -> asaltanteRepository.findById(idAsaltante)
                .filter(Asaltante::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "Asaltante", idAsaltante)))
                .toList();

        Sucursal sucursal = sucursalRepository.findById(dto.getSucursalId())
                .filter(Sucursal::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "Sucursal", dto.getSucursalId()));

        asalto.setAsaltantes(asaltantes);
        asalto.setSucursal(sucursal);

        return convertirADTO(asaltoRepository.save(asalto));
    }


    // Convierte una Entity en un DTO para devolver datos al Controller.
    private AsaltoDTO convertirADTO(Asalto asalto) {
        AsaltoDTO dto = new AsaltoDTO();
        dto.setId(asalto.getId());
        dto.setFecha(asalto.getFecha());
        dto.setAsaltantesIds(
                asalto.getAsaltantes().stream()
                        .map(Asaltante::getId)
                        .toList()
        );
        dto.setSucursalId(asalto.getSucursal().getId());
        dto.setCodigo(asalto.getCodigo());
        return dto;
    }

    // Convierte un DTO en una Entity para guardar o actualizar datos en la BBDD.
    private Asalto convertirAEntidad(AsaltoDTO dto) {
        Asalto asalto = new Asalto();

        asalto.setFecha(dto.getFecha());

        List<Asaltante> asaltantes = dto.getAsaltantesIds().stream()
                .map(id -> asaltanteRepository.findById(id)
                .filter(Asaltante::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Asaltante", id)))
                .toList();

        asalto.setAsaltantes(asaltantes);

        asalto.setSucursal(
                sucursalRepository.findById(dto.getSucursalId())
                        .filter(Sucursal::isActivo)
                        .orElseThrow(()
                                -> new RecursoNoEncontradoException(
                                "Sucursal", dto.getSucursalId()))
        );

        return asalto;
    }
}
