package prog2.policia_backend.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import prog2.policia_backend.DTOs.ContratoVigilanciaDTO;
import prog2.policia_backend.DTOs.UsuarioDTO;
import prog2.policia_backend.exceptions.ContratoVigilanciaCumplidoException;
import prog2.policia_backend.exceptions.ContratoVigilanciaDuplicadoException;
import prog2.policia_backend.exceptions.FechaPasadaException;
import prog2.policia_backend.exceptions.MotivoBajaObligatorioException;
import prog2.policia_backend.exceptions.PersonaInactivaException;
import prog2.policia_backend.exceptions.OperacionNoPermitidaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.exceptions.SucursalYaCerradaException;
import prog2.policia_backend.models.ContratoVigilancia;
import prog2.policia_backend.models.Entidad;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.models.Usuario;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;
import prog2.policia_backend.repositories.SucursalRepository;
import prog2.policia_backend.repositories.UsuarioRepository;
import prog2.policia_backend.repositories.RolRepository;
import prog2.policia_backend.security.AutorizadorPermisos;
import prog2.policia_backend.utils.GeneradorCodigo;

@Service
@RequiredArgsConstructor
public class ContratoVigilanciaService {

    private final ContratoVigilanciaRepository contratoVigilanciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;
    private final AutorizadorPermisos autorizadorPermisos;
    private final RolRepository rolRepository;

    public List<ContratoVigilanciaDTO> listar(Authentication authentication) {
        if (!autorizadorPermisos.puedeLeerTodo(authentication, Entidad.CONTRATO_VIGILANCIA)) {
            if (autorizadorPermisos.puedeLeerPropios(authentication, Entidad.CONTRATO_VIGILANCIA)) {
                return listarPorVigilante(authentication.getName());
            }
            throw new AccessDeniedException("No tiene permisos para consultar contratos");
        }

        List<ContratoVigilancia> contratos = contratoVigilanciaRepository.findAll();
        List<ContratoVigilanciaDTO> resultado = new ArrayList<>();
        for (ContratoVigilancia entidad : contratos) {
            resultado.add(convertirADTO(entidad));
        }
        return resultado;
    }

    public ContratoVigilanciaDTO buscarPorCodigo(String codigo) {
        return convertirADTO(obtenerContrato(codigo));
    }

    @Transactional
    public ContratoVigilanciaDTO guardar(ContratoVigilanciaDTO dto) {

        Usuario vigilante = obtenerVigilanteActivo(dto.getVigilanteCodigo());
        Sucursal sucursal = obtenerSucursalActiva(dto.getSucursalCodigo());
        ContratoVigilancia contrato = convertirAEntidad(dto, vigilante, sucursal);
        contrato = contratoVigilanciaRepository.save(contrato);
        contrato.setCodigo(GeneradorCodigo.generar("CDV", contrato.getId()));
        contrato.setActivo(true);

        contrato = contratoVigilanciaRepository.save(contrato);

        return convertirADTO(contrato);

    }

    @Transactional
    public ContratoVigilanciaDTO actualizar(String codigo, ContratoVigilanciaDTO dto) {

        ContratoVigilancia contrato = obtenerContrato(codigo);

        if (!contrato.getActivo()) {
            throw new ContratoVigilanciaCumplidoException();
        }

        if (dto.getFecha() != null && dto.getFecha().isBefore(LocalDate.now())) {
            throw new FechaPasadaException();
        }

        LocalDate fecha = dto.getFecha() != null ? dto.getFecha() : contrato.getFecha();

        Usuario vigilante;
        if (dto.getVigilanteCodigo() != null && !dto.getVigilanteCodigo().isBlank()) {
            vigilante = obtenerVigilanteActivo(dto.getVigilanteCodigo());
        } else {
            vigilante = obtenerVigilanteActivo(contrato.getVigilante().getCodigo());
        }

        boolean existeOtroContrato = estaOcupado(vigilante, fecha, contrato.getId());

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

    public void eliminar(String codigo, ContratoVigilanciaDTO dto) {

        ContratoVigilancia contrato = obtenerContrato(codigo);

        if (!contrato.getActivo() || contrato.getFecha().isBefore(LocalDate.now())) {
            throw new ContratoVigilanciaCumplidoException();
        }

        if (dto.getMotivoBaja() == null) {
            throw new MotivoBajaObligatorioException();
        }

        contrato.setActivo(false);
        contrato.setMotivoBaja(dto.getMotivoBaja());

        contratoVigilanciaRepository.save(contrato);
    }

    public List<ContratoVigilanciaDTO> listarPorVigilante(String codigo) {

        Usuario vigilante = obtenerVigilante(codigo);

        List<ContratoVigilancia> contratos = contratoVigilanciaRepository.findByVigilante_Id(vigilante.getId());

        List<ContratoVigilanciaDTO> resultado = new ArrayList<>();

        for (ContratoVigilancia contrato : contratos) {
            resultado.add(convertirADTO(contrato));
        }

        return resultado;
    }

    public List<ContratoVigilanciaDTO> listarPorSucursal(String codigo) {

        Sucursal sucursal = obtenerSucursal(codigo);

        List<ContratoVigilancia> contratos = contratoVigilanciaRepository.findBySucursal_Id(sucursal.getId());

        List<ContratoVigilanciaDTO> resultado = new ArrayList<>();

        for (ContratoVigilancia contrato : contratos) {
            resultado.add(convertirADTO(contrato));
        }

        return resultado;
    }

    private ContratoVigilanciaDTO convertirADTO(ContratoVigilancia contrato) {

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
                contrato.getModificadoPor());
    }

    private ContratoVigilancia convertirAEntidad(ContratoVigilanciaDTO dto, Usuario vigilante, Sucursal sucursal) {

        validarContratoDuplicado(vigilante, dto.getFecha());

        ContratoVigilancia contrato = new ContratoVigilancia();
        contrato.setFecha(dto.getFecha());
        contrato.setConArma(dto.getConArma());
        contrato.setVigilante(vigilante);
        contrato.setSucursal(sucursal);

        return contrato;
    }

    // ------Metodos Aux----
    private ContratoVigilancia obtenerContrato(String codigo) {
        Optional<ContratoVigilancia> resultado = contratoVigilanciaRepository.findByCodigo(codigo);

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("ContratoVigilancia", codigo);
        }

        return resultado.get();
    }

    private Usuario obtenerVigilante(String codigo) {
        Optional<Usuario> resultado = usuarioRepository.findByCodigo(codigo);

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Vigilante", codigo);
        }

        return resultado.get();
    }

    private Usuario obtenerVigilanteActivo(String codigo) {
        Usuario vigilante = obtenerVigilante(codigo);

        if (vigilante.getRol() == null || !vigilante.getRol().isHabilitadoParaVigilancia()) {
            throw new RecursoNoEncontradoException("Vigilante habilitado", codigo);
        }
        if (!vigilante.isActivo() || !Boolean.TRUE.equals(vigilante.getRol().getActivo())) {
            throw new PersonaInactivaException();
        }
        // Revalida con bloqueo para coordinar la asignación con el retiro de la habilitación.
        if (rolRepository.findConBloqueoParaVigilanciaByUsuarioCodigo(codigo).isEmpty()) {
            throw new OperacionNoPermitidaException("El vigilante debe tener usuario y rol activos y habilitación para vigilancia.");
        }

        return vigilante;
    }

    private void validarContratoDuplicado(Usuario vigilante, LocalDate fecha) {
        if (estaOcupado(vigilante, fecha, null)) {
            throw new ContratoVigilanciaDuplicadoException();
        }
    }

    private boolean estaOcupado(Usuario vigilante, LocalDate fecha, Long contratoId) {
        if (contratoId != null) {
            return contratoVigilanciaRepository.existsByVigilante_IdAndFechaAndActivoTrueAndIdNot(
                    vigilante.getId(), fecha, contratoId);
        }
        return contratoVigilanciaRepository.existsByVigilante_IdAndFechaAndActivoTrue(vigilante.getId(), fecha);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarVigilantesDisponibles(LocalDate fecha, String contratoCodigo) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new FechaPasadaException();
        }
        Long contratoId = null;
        if (contratoCodigo != null && !contratoCodigo.isBlank()) {
            contratoId = obtenerContrato(contratoCodigo).getId();
        }
        List<UsuarioDTO> resultado = new ArrayList<>();
        for (Usuario vigilante : usuarioRepository.findByActivoTrueAndRol_ActivoTrueAndRol_HabilitadoParaVigilanciaTrue()) {
            if (!estaOcupado(vigilante, fecha, contratoId)) {
                UsuarioDTO dto = new UsuarioDTO();
                dto.setCodigo(vigilante.getCodigo());
                dto.setNombre(vigilante.getNombre());
                dto.setRol(vigilante.getRol().getNombre());
                dto.setActivo(true);
                resultado.add(dto);
            }
        }
        return resultado;
    }

    private Sucursal obtenerSucursal(String codigo) {
        Optional<Sucursal> resultado = sucursalRepository.findByCodigo(codigo);

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Sucursal", codigo);
        }

        return resultado.get();
    }

    private Sucursal obtenerSucursalActiva(String codigo) {
        Sucursal sucursal = obtenerSucursal(codigo);

        if (!sucursal.isActivo()) {
            throw new SucursalYaCerradaException();
        }

        return sucursal;
    }

    public List<ContratoVigilanciaDTO> filtrarPorPermisos(List<ContratoVigilanciaDTO> contratos, Authentication authentication) {

        if (autorizadorPermisos.puedeLeerTodo(authentication, Entidad.CONTRATO_VIGILANCIA)) {
            return contratos;
        }

        if (!autorizadorPermisos.puedeLeerPropios(authentication, Entidad.CONTRATO_VIGILANCIA)) {
            throw new AccessDeniedException("No tiene permisos para consultar contratos");
        }

        List<ContratoVigilanciaDTO> resultado = new ArrayList<>();

        for (ContratoVigilanciaDTO contrato : contratos) {
            if (authentication.getName().equals(contrato.getVigilanteCodigo())) {
                resultado.add(contrato);
            }
        }

        return resultado;
    }

}
