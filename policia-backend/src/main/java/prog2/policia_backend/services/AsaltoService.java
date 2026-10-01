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
        return asaltoRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public AsaltoDTO buscarPorId(Long id) {
        return asaltoRepository.findById(id)
                .filter(Asalto::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asalto", id));
    }

    public AsaltoDTO guardar(AsaltoDTO dto) {
        Asalto asalto = convertirAEntidad(dto);

        asalto = asaltoRepository.save(asalto);

        asalto.setCodigo(
                GeneradorCodigo.generar("AST", asalto.getId())
        );

        return convertirADTO(asaltoRepository.save(asalto));
    }

    public AsaltoDTO actualizar(Long id, AsaltoDTO dto) {
        Asalto asalto = asaltoRepository.findById(id)
                .filter(Asalto::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asalto", id));

        asalto.setFecha(dto.getFecha());

        Asaltante asaltante = asaltanteRepository.findById(dto.getAsaltanteId())
                .filter(Asaltante::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Asaltante", dto.getAsaltanteId()));

        Sucursal sucursal = sucursalRepository.findById(dto.getSucursalId())
                .filter(Sucursal::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Sucursal", dto.getSucursalId()));

        asalto.setAsaltante(asaltante);
        asalto.setSucursal(sucursal);

        return convertirADTO(asaltoRepository.save(asalto));
    }

    public void eliminar(Long id) {
        Asalto asalto = asaltoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asalto", id));

        asalto.setActivo(false);
        asaltoRepository.save(asalto);
    }

    private AsaltoDTO convertirADTO(Asalto asalto) {
        return new AsaltoDTO(
                asalto.getId(),
                asalto.getFecha(),
                asalto.getAsaltante().getId(),
                asalto.getSucursal().getId(),
                asalto.getCodigo()
        );
    }

    private Asalto convertirAEntidad(AsaltoDTO dto) {
        Asalto asalto = new Asalto();

        asalto.setFecha(dto.getFecha());

        asalto.setAsaltante(
                asaltanteRepository.findById(dto.getAsaltanteId())
                        .filter(Asaltante::isActivo)
                        .orElseThrow(()
                                -> new RecursoNoEncontradoException(
                                "Asaltante", dto.getAsaltanteId()))
        );

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
