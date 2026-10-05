package prog2.policia_backend.services;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import prog2.policia_backend.DTOs.AsaltoDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Asalto;
import prog2.policia_backend.repositories.AsaltoRepository;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.SucursalRepository;
import prog2.policia_backend.DTOs.AsaltanteDTO;
import prog2.policia_backend.exceptions.PersonaInactivaException;
import prog2.policia_backend.exceptions.SucursalYaCerradaException;
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

    public List<AsaltoDTO> listarPorAsaltante(String codigo) {

        Asaltante asaltante = asaltanteRepository.findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "Asaltante", codigo));

        return asaltoRepository
                .findByAsaltantes_Id(asaltante.getId())
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public List<AsaltoDTO> listarPorSucursal(String codigo) {

        Sucursal sucursal = sucursalRepository.findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "Sucursal", codigo));

        return asaltoRepository
                .findBySucursal_Id(sucursal.getId())
                .stream()
                .map(this::convertirADTO)
                .toList();
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
                .orElseThrow(() -> new RecursoNoEncontradoException("Asalto", codigo));

        if (dto.getFecha() != null) {
            asalto.setFecha(dto.getFecha());
        }

        if (dto.getAsaltantesCodigos() != null && !dto.getAsaltantesCodigos().isEmpty()) {
            List<Asaltante> nuevosAsaltantes = dto.getAsaltantesCodigos().stream()
                    .map(this::obtenerAsaltanteActivo)
                    .collect(Collectors.toList());

            asalto.getAsaltantes().clear();
            asalto.getAsaltantes().addAll(nuevosAsaltantes);
        }

        if (dto.getSucursalCodigo() != null && !dto.getSucursalCodigo().isBlank()) {
            asalto.setSucursal(obtenerSucursalActiva(dto.getSucursalCodigo()));
        }

        return convertirADTO(asaltoRepository.save(asalto));
    }

    // Convierte una Entity en un DTO para devolver datos al Controller.
    private AsaltoDTO convertirADTO(Asalto asalto) {
        AsaltoDTO dto = new AsaltoDTO();
        dto.setId(asalto.getId());
        dto.setFecha(asalto.getFecha());
        dto.setAsaltantesCodigos(
                asalto.getAsaltantes().stream()
                        .map(Asaltante::getCodigo) // <-- Devuelves los códigos
                        .toList()
        );
        dto.setSucursalCodigo(asalto.getSucursal().getCodigo());
        dto.setAsaltantes(
                asalto.getAsaltantes().stream()
                        .map(asaltante -> new AsaltanteDTO(
                        asaltante.getId(),
                        asaltante.getNombre(),
                        asaltante.getBanda() != null
                        ? asaltante.getBanda().getCodigo()
                        : null,
                        asaltante.getCodigo(),
                        asaltante.getMotivoBaja(),
                        null, //quitar de banda
                        asaltante.getFechaCreacion(),
                        asaltante.getFechaModificacion(),
                        asaltante.getCreadoPor(),
                        asaltante.getModificadoPor()
                ))
                        .toList()
        );
        dto.setSucursalCodigo(asalto.getSucursal().getCodigo());
        dto.setCodigo(asalto.getCodigo());
        dto.setFechaCreacion(asalto.getFechaCreacion());
        dto.setFechaModificacion(asalto.getFechaModificacion());
        dto.setCreadoPor(asalto.getCreadoPor());
        dto.setModificadoPor(asalto.getModificadoPor());

        return dto;
    }

    // Convierte un DTO en una Entity para guardar o actualizar datos en la BBDD.
    private Asalto convertirAEntidad(AsaltoDTO dto) {
        Asalto asalto = new Asalto();
        asalto.setFecha(dto.getFecha());

        List<Asaltante> asaltantes = dto.getAsaltantesCodigos().stream()
                .map(this::obtenerAsaltanteActivo)
                .collect(Collectors.toList());
        asalto.setAsaltantes(asaltantes);

        asalto.setSucursal(obtenerSucursalActiva(dto.getSucursalCodigo()));

        return asalto;
    }

    //-------Metodos Auxiliares---------
    private Asaltante obtenerAsaltanteActivo(String codigo) {
        Asaltante asaltante = asaltanteRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asaltante", codigo));
        if (!asaltante.isActivo()) {
            throw new PersonaInactivaException();
        }
        return asaltante;
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
