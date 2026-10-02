package prog2.policia_backend.services;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import prog2.policia_backend.DTOs.SucursalDTO;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.ContratoVigilancia;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.repositories.EntidadBancariaRepository;
import prog2.policia_backend.repositories.SucursalRepository;
import prog2.policia_backend.models.EntidadBancaria;
import prog2.policia_backend.models.MotivoBajaContrato;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;
import prog2.policia_backend.utils.GeneradorCodigo;

@Service
@RequiredArgsConstructor
public class SucursalService {

    private final SucursalRepository sucursalRepository;
    private final EntidadBancariaRepository entidadBancariaRepository;
    private final ContratoVigilanciaRepository contratoVigilanciaRepository;

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

        sucursal = sucursalRepository.save(sucursal);

        sucursal.setCodigo(
                GeneradorCodigo.generar("SUC", sucursal.getId())
        );

        sucursal = sucursalRepository.save(sucursal);

        return convertirADTO(sucursal);
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

    @Transactional //Si falla la baja de alguno de los contratos o la suc, se revierte la operación
    public void eliminar(Long id) {

        Sucursal sucursal = sucursalRepository.findById(id)
                .filter(Sucursal::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Sucursal", id));

        List<ContratoVigilancia> contratosFuturos
                = contratoVigilanciaRepository
                        .findBySucursal_IdAndFechaAfterAndActivoTrue(
                                id,
                                LocalDate.now());

        contratosFuturos.forEach(contrato -> {
            contrato.setActivo(false);
            contrato.setMotivoBaja(MotivoBajaContrato.CIERRE_SUCURSAL);
        });

        contratoVigilanciaRepository.saveAll(contratosFuturos);

        sucursal.setActivo(false);
        sucursalRepository.save(sucursal);
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
