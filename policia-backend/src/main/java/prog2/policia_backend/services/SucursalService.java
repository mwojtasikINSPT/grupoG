package prog2.policia_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import prog2.policia_backend.DTOs.SucursalDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.repositories.EntidadBancariaRepository;
import prog2.policia_backend.repositories.SucursalRepository;

import java.util.List;
import prog2.policia_backend.models.EntidadBancaria;

@Service
@RequiredArgsConstructor
public class SucursalService {

    private final SucursalRepository sucursalRepository;
    private final EntidadBancariaRepository entidadBancariaRepository;

    public List<SucursalDTO> listar() {
        return sucursalRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public SucursalDTO buscarPorId(Long id) {
        return sucursalRepository.findById(id)
                .filter(Sucursal::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", id));
    }

    public SucursalDTO guardar(SucursalDTO dto) {
        Sucursal sucursal = convertirAEntidad(dto);
        return convertirADTO(sucursalRepository.save(sucursal));
    }

    public SucursalDTO actualizar(Long id, SucursalDTO dto) {
        Sucursal sucursal = sucursalRepository.findById(id)
                .filter(Sucursal::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", id));

        sucursal.setDomicilio(dto.getDomicilio());
        sucursal.setCantEmpleados(dto.getCantEmpleados());

        EntidadBancaria entidad = entidadBancariaRepository.findById(dto.getEntidadBancariaId())
                .filter(EntidadBancaria::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "EntidadBancaria",
                        dto.getEntidadBancariaId()));

        sucursal.setEntidadBancaria(entidad);

        return convertirADTO(sucursalRepository.save(sucursal));
    }

    public void eliminar(Long id) {
        if (!sucursalRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Sucursal", id);
        }

        sucursalRepository.deleteById(id);
    }

    private SucursalDTO convertirADTO(Sucursal sucursal) {
        return new SucursalDTO(
                sucursal.getId(),
                sucursal.getDomicilio(),
                sucursal.getCantEmpleados(),
                sucursal.getEntidadBancaria().getId(),
                sucursal.getCodigo()
        );
    }

    private Sucursal convertirAEntidad(SucursalDTO dto) {
        Sucursal sucursal = new Sucursal();

        sucursal.setDomicilio(dto.getDomicilio());
        sucursal.setCantEmpleados(dto.getCantEmpleados());

        sucursal.setEntidadBancaria(
                entidadBancariaRepository.findById(dto.getEntidadBancariaId())
                        .filter(EntidadBancaria::isActivo)
                        .orElseThrow(()
                                -> new RecursoNoEncontradoException(
                                "EntidadBancaria",
                                dto.getEntidadBancariaId()))
        );

        return sucursal;
    }
}
