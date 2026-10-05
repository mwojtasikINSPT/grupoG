package prog2.policia_backend.services;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import prog2.policia_backend.DTOs.SucursalDTO;
import prog2.policia_backend.exceptions.EntidadInactivaException;
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
import prog2.policia_backend.utils.NormalizadorTexto;

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

    public List<SucursalDTO> buscarPorDomicilio(
            String domicilio,
            Boolean activo) {

        String domicilioNormalizado
                = NormalizadorTexto.normalizarParaBuscar(domicilio);

        return sucursalRepository.findAll()
                .stream()
                .filter(sucursal
                        -> activo == null
                || sucursal.isActivo() == activo)
                .filter(sucursal
                        -> NormalizadorTexto.normalizarParaBuscar(
                        sucursal.getDomicilio()
                ).contains(domicilioNormalizado))
                .map(this::convertirADTO)
                .toList();
    }

    public List<SucursalDTO> listarPorEntidadBancaria(String codigo) {

        EntidadBancaria entidad = entidadBancariaRepository
                .findByCodigo(codigo)
                .orElseThrow(()
                        -> new RecursoNoEncontradoException(
                        "EntidadBancaria", codigo));

        return sucursalRepository
                .findByEntidadBancaria_Id(entidad.getId())
                .stream()
                .map(this::convertirADTO)
                .toList();
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

        if (dto.getDomicilio() != null && !dto.getDomicilio().isBlank()) {
            sucursal.setDomicilio(NormalizadorTexto.normalizarParaGuardar(dto.getDomicilio()));
        }

        if (dto.getCantEmpleados() != null) {
            sucursal.setCantEmpleados(dto.getCantEmpleados());
        }

        if (dto.getEntidadBancariaCodigo() != null) {
            sucursal.setEntidadBancaria(obtenerEntidadBancariaActiva(dto.getEntidadBancariaCodigo()));
        }

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
                sucursal.getEntidadBancaria().getCodigo(),
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

        sucursal.setDomicilio(NormalizadorTexto.normalizarParaGuardar(dto.getDomicilio()));
        sucursal.setCantEmpleados(dto.getCantEmpleados());

        sucursal.setEntidadBancaria(obtenerEntidadBancariaActiva(dto.getEntidadBancariaCodigo()));

        return sucursal;
    }

    //-----Metodos Aux----
    private EntidadBancaria obtenerEntidadBancariaActiva(String codigo) {
        EntidadBancaria entidad = entidadBancariaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("EntidadBancaria", codigo));

        if (!entidad.isActivo()) {
            throw new EntidadInactivaException();
        }
        return entidad;
    }

}
