package prog2.policia_backend.services;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import prog2.policia_backend.DTOs.SucursalDTO;
import prog2.policia_backend.exceptions.MotivoCierreSucursalObligatorioException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.exceptions.SucursalNoReactivableException;
import prog2.policia_backend.exceptions.SucursalYaActivaException;
import prog2.policia_backend.exceptions.SucursalYaCerradaException;
import prog2.policia_backend.models.ContratoVigilancia;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.repositories.EntidadBancariaRepository;
import prog2.policia_backend.repositories.SucursalRepository;
import prog2.policia_backend.models.EntidadBancaria;
import prog2.policia_backend.models.MotivoBajaContrato;
import prog2.policia_backend.models.MotivoCierreSucursal;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;
import prog2.policia_backend.utils.GeneradorCodigo;

@Service
@RequiredArgsConstructor
public class SucursalService {

    private final SucursalRepository sucursalRepository;
    private final EntidadBancariaRepository entidadBancariaRepository;
    private final ContratoVigilanciaRepository contratoVigilanciaRepository;

    public List<SucursalDTO> listar(Boolean activo) {

        List<Sucursal> sucursales;

        if (activo == null) {
            sucursales = sucursalRepository.findAll();
        } else {
            sucursales = sucursalRepository.findByActivo(activo);
        }

        return sucursales.stream()
                .map(this::convertirADTO)
                .toList();
    }

    public SucursalDTO buscarPorId(Long id) {
        return sucursalRepository.findById(id)
                .filter(Sucursal::isActivo)
                .map(this::convertirADTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", id));
    }

    public SucursalDTO buscarPorCodigo(String codigo) {
        return sucursalRepository.findByCodigo(codigo)
                .map(this::convertirADTO)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Sucursal", codigo));
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

    public SucursalDTO actualizar(String codigo, SucursalDTO dto) {
        Sucursal sucursal = sucursalRepository.findByCodigo(codigo)
                .filter(Sucursal::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Sucursal", codigo));

        sucursal.setDomicilio(dto.getDomicilio());
        sucursal.setCantEmpleados(dto.getCantEmpleados());

        EntidadBancaria entidad = entidadBancariaRepository
                .findById(dto.getEntidadBancariaId())
                .filter(EntidadBancaria::isActivo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "EntidadBancaria",
                        dto.getEntidadBancariaId()));

        sucursal.setEntidadBancaria(entidad);

        return convertirADTO(sucursalRepository.save(sucursal));
    }

    @Transactional //Si falla la baja de alguno de los contratos o la suc, se revierte la operación
    public void eliminar(String codigo, SucursalDTO dto) {

        Sucursal sucursal = sucursalRepository.findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException("Sucursal", codigo));

        if (!sucursal.isActivo()) {
            throw new SucursalYaCerradaException();
        }

        if (dto.getMotivoCierre() == null) {
            throw new MotivoCierreSucursalObligatorioException();
        }

        List<ContratoVigilancia> contratosFuturos
                = contratoVigilanciaRepository
                        .findBySucursal_IdAndFechaAfterAndActivoTrue(
                                sucursal.getId(),
                                LocalDate.now());

        contratosFuturos.forEach(contrato -> {
            contrato.setActivo(false);
            contrato.setMotivoBaja(MotivoBajaContrato.CIERRE_SUCURSAL);
        });

        contratoVigilanciaRepository.saveAll(contratosFuturos);

        sucursal.setActivo(false);

        sucursal.setMotivoCierre(dto.getMotivoCierre());
        sucursalRepository.save(sucursal);
    }

    public SucursalDTO reactivar(String codigo) {

        Sucursal sucursal = sucursalRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                "Sucursal", codigo));

        if (sucursal.isActivo()) {
            throw new SucursalYaActivaException();
        }

        if (sucursal.getMotivoCierre()
                == MotivoCierreSucursal.CIERRE_DEFINITIVO) {

            throw new SucursalNoReactivableException();
        }

        sucursal.setActivo(true);
        sucursal.setMotivoCierre(null);

        return convertirADTO(sucursalRepository.save(sucursal));
    }

    private SucursalDTO convertirADTO(Sucursal sucursal) {
        return new SucursalDTO(
                sucursal.getId(),
                sucursal.getDomicilio(),
                sucursal.getCantEmpleados(),
                sucursal.getEntidadBancaria().getId(),
                sucursal.getCodigo(),
                sucursal.getMotivoCierre(),
                sucursal.getFechaCreacion(),
                sucursal.getFechaModificacion(),
                sucursal.getCreadoPor(),
                sucursal.getModificadoPor()
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
