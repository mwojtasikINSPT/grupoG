package prog2.policia_backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import prog2.policia_backend.DTOs.AsaltoDTO;
import prog2.policia_backend.exceptions.AsaltanteConCasoJudicialException;
import prog2.policia_backend.exceptions.AsaltanteDuplicadoException;
import prog2.policia_backend.exceptions.OperacionNoPermitidaException;
import prog2.policia_backend.exceptions.PersonaInactivaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.exceptions.SucursalYaCerradaException;
import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.models.Asalto;
import prog2.policia_backend.models.CasoJudicial;
import prog2.policia_backend.models.SentenciaAsaltante;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.AsaltoRepository;
import prog2.policia_backend.repositories.CasoJudicialRepository;
import prog2.policia_backend.repositories.SentenciaAsaltanteRepository;
import prog2.policia_backend.repositories.SucursalRepository;
import prog2.policia_backend.utils.GeneradorCodigo;

@Service
@RequiredArgsConstructor
public class AsaltoService {

    private final AsaltoRepository asaltoRepository;
    private final AsaltanteRepository asaltanteRepository;
    private final SucursalRepository sucursalRepository;
    private final CasoJudicialRepository casoJudicialRepository;
    private final SentenciaAsaltanteRepository sentenciaAsaltanteRepository;

    @Transactional(readOnly = true)
    public List<AsaltoDTO> listar() {
        List<AsaltoDTO> resultado = new ArrayList<>();

        for (Asalto asalto : asaltoRepository.findAll()) {
            resultado.add(convertirADTO(asalto));
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public AsaltoDTO buscarPorCodigo(String codigo) {
        return convertirADTO(obtenerAsalto(codigo));
    }

    @Transactional(readOnly = true)
    public List<AsaltoDTO> listarPorAsaltante(String codigo) {
        Asaltante asaltante = obtenerAsaltante(codigo);
        List<AsaltoDTO> resultado = new ArrayList<>();

        for (Asalto asalto : asaltoRepository.findByAsaltantes_Id(asaltante.getId())) {
            resultado.add(convertirADTO(asalto));
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public List<AsaltoDTO> listarPorSucursal(String codigo) {
        Sucursal sucursal = obtenerSucursal(codigo);
        List<AsaltoDTO> resultado = new ArrayList<>();

        for (Asalto asalto : asaltoRepository.findBySucursal_Id(sucursal.getId())) {
            resultado.add(convertirADTO(asalto));
        }

        return resultado;
    }

    @Transactional
    public AsaltoDTO guardar(AsaltoDTO dto) {
        List<Asaltante> asaltantes = obtenerAsaltantesActivos(dto.getAsaltantesCodigos());
        Sucursal sucursal = obtenerSucursalActiva(dto.getSucursalCodigo());

        Asalto asalto = new Asalto(dto.getFecha(), sucursal, asaltantes);
        asalto = asaltoRepository.save(asalto);

        asalto.asignarCodigo(GeneradorCodigo.generar("AST", asalto.getId()));

        return convertirADTO(asaltoRepository.save(asalto));
    }

    @Transactional
    public AsaltoDTO actualizar(String codigo, AsaltoDTO dto) {
        Asalto asalto = obtenerAsalto(codigo);
        CasoJudicial caso = obtenerCasoJudicial(asalto);

        if (dto.getSucursalCodigo() != null && !dto.getSucursalCodigo().isBlank()) {
            Sucursal nuevaSucursal = obtenerSucursalActiva(dto.getSucursalCodigo());

            if (!asalto.getSucursal().getId().equals(nuevaSucursal.getId())) {
                if (caso != null) {
                    throw new OperacionNoPermitidaException("No se puede cambiar la sucursal de un asalto con caso judicial");
                }

                asalto.cambiarSucursal(nuevaSucursal);
            }
        }

        if (dto.getAsaltantesCodigos() != null) {
            List<Asaltante> nuevosAsaltantes = obtenerAsaltantesActivos(dto.getAsaltantesCodigos());

            for (Asaltante existente : asalto.getAsaltantes()) {
                if (!contieneAsaltante(nuevosAsaltantes, existente.getId())) {
                    if (caso != null) {
                        throw new AsaltanteConCasoJudicialException();
                    }

                    asalto.quitarAsaltante(existente);
                    break;
                }
            }

            if (caso == null) {
                List<Asaltante> asaltantesAQuitar = new ArrayList<>();

                for (Asaltante existente : asalto.getAsaltantes()) {
                    if (!contieneAsaltante(nuevosAsaltantes, existente.getId())) {
                        asaltantesAQuitar.add(existente);
                    }
                }

                for (Asaltante asaltante : asaltantesAQuitar) {
                    asalto.quitarAsaltante(asaltante);
                }
            }

            boolean participantesAgregados = false;

            for (Asaltante nuevo : nuevosAsaltantes) {
                if (!contieneAsaltante(asalto.getAsaltantes(), nuevo.getId())) {
                    asalto.agregarAsaltante(nuevo);
                    participantesAgregados = true;

                    if (caso != null) {
                        SentenciaAsaltante sentencia = new SentenciaAsaltante(caso, nuevo);
                        sentenciaAsaltanteRepository.save(sentencia);
                    }
                }
            }

            if (caso != null && participantesAgregados) {
                caso.actualizarEstadoSentenciado(false);
                casoJudicialRepository.save(caso);
            }
        }

        return convertirADTO(asaltoRepository.save(asalto));
    }

    // Convierte una Entity en un DTO para devolver datos al Controller.
    private AsaltoDTO convertirADTO(Asalto asalto) {
        AsaltoDTO dto = new AsaltoDTO();

        dto.setId(asalto.getId());
        dto.setCodigo(asalto.getCodigo());
        dto.setFecha(asalto.getFecha());
        dto.setActivo(true);

        if (asalto.getSucursal() != null) {
            dto.setSucursalCodigo(asalto.getSucursal().getCodigo());
        }

        List<String> codigos = new ArrayList<>();

        if (asalto.getAsaltantes() != null) {
            for (Asaltante asaltante : asalto.getAsaltantes()) {
                codigos.add(asaltante.getCodigo());
            }
        }

        dto.setAsaltantesCodigos(codigos);
        dto.setFechaCreacion(asalto.getFechaCreacion());
        dto.setFechaModificacion(asalto.getFechaModificacion());
        dto.setCreadoPor(asalto.getCreadoPor());
        dto.setModificadoPor(asalto.getModificadoPor());

        return dto;
    }

    // ------- Metodos Auxiliares ---------
    private Asalto obtenerAsalto(String codigo) {
        Optional<Asalto> resultado = asaltoRepository.findByCodigo(codigo);

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Asalto", codigo);
        }

        return resultado.get();
    }

    private Asaltante obtenerAsaltante(String codigo) {
        Optional<Asaltante> resultado = asaltanteRepository.findByCodigo(codigo);

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Asaltante", codigo);
        }

        return resultado.get();
    }

    private Asaltante obtenerAsaltanteActivo(String codigo) {
        Asaltante asaltante = obtenerAsaltante(codigo);

        if (!asaltante.isActivo()) {
            throw new PersonaInactivaException();
        }

        return asaltante;
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

    private List<Asaltante> obtenerAsaltantesActivos(List<String> codigos) {
        List<Asaltante> resultado = new ArrayList<>();

        if (codigos == null) {
            return resultado;
        }

        validarAsaltantesDuplicados(codigos);

        for (String codigo : codigos) {
            resultado.add(obtenerAsaltanteActivo(codigo));
        }

        return resultado;
    }

    private void validarAsaltantesDuplicados(List<String> codigos) {
        for (int i = 0; i < codigos.size(); i++) {
            for (int j = i + 1; j < codigos.size(); j++) {
                if (codigos.get(i).equals(codigos.get(j))) {
                    throw new AsaltanteDuplicadoException();
                }
            }
        }
    }

    private boolean contieneAsaltante(List<Asaltante> asaltantes, Long id) {
        for (Asaltante asaltante : asaltantes) {
            if (asaltante.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    private CasoJudicial obtenerCasoJudicial(Asalto asalto) {
        return asalto.getCasoJudicial();
    }
}
