package prog2.policia_backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import prog2.policia_backend.DTOs.CasoJudicialDTO;
import prog2.policia_backend.DTOs.SentenciaAsaltanteDTO;
import prog2.policia_backend.exceptions.AsaltanteNoParticipaEnAsaltoException;
import prog2.policia_backend.exceptions.CasoCondenadoSinCarcelException;
import prog2.policia_backend.exceptions.CasoJudicialActivoException;
import prog2.policia_backend.exceptions.CasoJudicialYaExistenteException;
import prog2.policia_backend.exceptions.CasoSentenciadoException;
import prog2.policia_backend.exceptions.CasoSinCondenaConCarcelException;
import prog2.policia_backend.exceptions.DatosCondenaInvalidosException;
import prog2.policia_backend.exceptions.OperacionNoPermitidaException;
import prog2.policia_backend.exceptions.PersonaInactivaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.models.Asalto;
import prog2.policia_backend.models.CasoJudicial;
import prog2.policia_backend.models.EstadoCasoJudicial;
import prog2.policia_backend.models.EstadoParticipacionJudicial;
import prog2.policia_backend.models.Juez;
import prog2.policia_backend.models.SentenciaAsaltante;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.AsaltoRepository;
import prog2.policia_backend.repositories.CasoJudicialRepository;
import prog2.policia_backend.repositories.JuezRepository;
import prog2.policia_backend.repositories.SentenciaAsaltanteRepository;
import prog2.policia_backend.utils.GeneradorCodigo;

// Gestiona los casos judiciales, sus jueces, estados y consultas.
@Service
@RequiredArgsConstructor
public class CasoJudicialService {

    private final CasoJudicialRepository casoJudicialRepository;
    private final AsaltoRepository asaltoRepository;
    private final JuezRepository juezRepository;
    private final AsaltanteRepository asaltanteRepository;
    private final SentenciaAsaltanteRepository sentenciaAsaltanteRepository;

    @Transactional(readOnly = true)
    public List<CasoJudicialDTO> listar(Boolean activo) {
        List<CasoJudicial> casos;

        if (activo == null) {
            casos = casoJudicialRepository.findAll();
        } else {
            casos = casoJudicialRepository.findByActivo(activo);
        }

        List<CasoJudicialDTO> resultado = new ArrayList<>();

        for (CasoJudicial caso : casos) {
            resultado.add(convertirADTO(caso));
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public CasoJudicialDTO buscarPorCodigo(String codigo) {
        return convertirADTO(obtenerCaso(codigo));
    }

    @Transactional
    public CasoJudicialDTO guardar(CasoJudicialDTO dto) {
        Asalto asalto = obtenerAsalto(dto.getAsaltoCodigo());
        Juez juez = obtenerJuezActivo(dto.getJuezCodigo());

        if (casoJudicialRepository.existsByAsalto_Id(asalto.getId())) {
            throw new CasoJudicialYaExistenteException();
        }

        if (asalto.getAsaltantes() == null || asalto.getAsaltantes().isEmpty()) {
            throw new OperacionNoPermitidaException("El asalto debe tener al menos un asaltante");
        }

        for (Asaltante asaltante : asalto.getAsaltantes()) {
            if (!asaltante.isActivo()) {
                throw new PersonaInactivaException();
            }
        }

        CasoJudicial caso = new CasoJudicial(null, asalto, juez);
        caso = casoJudicialRepository.saveAndFlush(caso);

        caso.asignarCodigo(GeneradorCodigo.generar("CJU", caso.getId()));

        List<SentenciaAsaltante> sentencias = new ArrayList<>();

        for (Asaltante asaltante : asalto.getAsaltantes()) {
            sentencias.add(new SentenciaAsaltante(caso, asaltante));
        }

        sentenciaAsaltanteRepository.saveAll(sentencias);

        return convertirADTO(casoJudicialRepository.save(caso));
    }

    @Transactional
    public CasoJudicialDTO actualizar(String codigo, CasoJudicialDTO dto) {
        CasoJudicial caso = obtenerCaso(codigo);

        if (!caso.isActivo()) {
            throw new OperacionNoPermitidaException("El caso judicial está inactivo");
        }

        if (dto.getJuezCodigo() != null && !dto.getJuezCodigo().isBlank()) {
            Juez juez = obtenerJuezActivo(dto.getJuezCodigo());
            caso.cambiarJuez(juez);
        }

        return convertirADTO(casoJudicialRepository.save(caso));
    }

    @Transactional(readOnly = true)
    public List<CasoJudicialDTO> listarPorAsaltante(String codigo) {
        Asaltante asaltante = obtenerAsaltante(codigo);
        List<CasoJudicial> casos = casoJudicialRepository.findBySentencias_Asaltante_Id(asaltante.getId());
        List<CasoJudicialDTO> resultado = new ArrayList<>();

        for (CasoJudicial caso : casos) {
            resultado.add(convertirADTO(caso));
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public List<CasoJudicialDTO> listarPorJuez(String codigo) {
        Juez juez = obtenerJuez(codigo);
        List<CasoJudicial> casos = casoJudicialRepository.findByJuez_Id(juez.getId());
        List<CasoJudicialDTO> resultado = new ArrayList<>();

        for (CasoJudicial caso : casos) {
            resultado.add(convertirADTO(caso));
        }

        return resultado;
    }

    @Transactional
    public void eliminar(String codigo) {
        CasoJudicial caso = obtenerCaso(codigo);

        if (caso.getEstado() != EstadoCasoJudicial.REABIERTO_POR_APELACION) {
            throw new OperacionNoPermitidaException("Solo se puede cerrar manualmente un caso reabierto por apelación");
        }

        caso.actualizarActivo(false);
        caso.actualizarEstado(EstadoCasoJudicial.CERRADO);

        casoJudicialRepository.save(caso);
    }

    @Transactional
    public CasoJudicialDTO reactivar(String codigo, String motivo) {
        CasoJudicial caso = obtenerCaso(codigo);

        if (caso.getEstado() != EstadoCasoJudicial.CERRADO) {
            throw new OperacionNoPermitidaException("Solo se pueden reabrir casos cerrados");
        }

        if (motivo == null || motivo.isBlank()) {
            throw new OperacionNoPermitidaException("Debe indicar el motivo de la apelación");
        }

        caso.reabrirPorApelacion(motivo);

        return convertirADTO(casoJudicialRepository.save(caso));
    }

    @Transactional
    public void actualizarEstadoSentenciado(Long casoId) {
        actualizarEstadoCaso(casoId);
    }

    private CasoJudicialDTO convertirADTO(CasoJudicial caso) {
        CasoJudicialDTO dto = new CasoJudicialDTO();

        dto.setId(caso.getId());
        dto.setCodigo(caso.getCodigo());
        dto.setActivo(caso.isActivo());
        dto.setSentenciado(caso.isSentenciado());
        dto.setAsaltoCodigo(caso.getAsalto().getCodigo());
        dto.setJuezCodigo(caso.getJuez().getCodigo());
        dto.setEstado(caso.getEstado());
        dto.setMotivoReapertura(caso.getMotivoReapertura());

        List<String> codigos = new ArrayList<>();

        for (Asaltante asaltante : caso.getAsalto().getAsaltantes()) {
            codigos.add(asaltante.getCodigo());
        }

        dto.setAsaltanteCodigos(codigos);
        dto.setFechaCreacion(caso.getFechaCreacion());
        dto.setFechaModificacion(caso.getFechaModificacion());
        dto.setCreadoPor(caso.getCreadoPor());
        dto.setModificadoPor(caso.getModificadoPor());

        return dto;
    }

    private CasoJudicial obtenerCaso(String codigo) {
        Optional<CasoJudicial> resultado = casoJudicialRepository.findByCodigo(codigo);

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("CasoJudicial", codigo);
        }

        return resultado.get();
    }

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

    private Juez obtenerJuez(String codigo) {
        Optional<Juez> resultado = juezRepository.findByCodigo(codigo);

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Juez", codigo);
        }

        return resultado.get();
    }

    private Juez obtenerJuezActivo(String codigo) {
        Juez juez = obtenerJuez(codigo);

        if (!juez.isActivo()) {
            throw new PersonaInactivaException();
        }

        return juez;
    }

    @Transactional
    public void registrarSentencia(String casoCodigo, String asaltanteCodigo, boolean condenado, int tiempoCarcel) {
        CasoJudicial caso = obtenerCaso(casoCodigo);
        Asaltante asaltante = obtenerAsaltante(asaltanteCodigo);

        if (!caso.isActivo()) {
            throw new OperacionNoPermitidaException("El caso judicial está inactivo");
        }

        boolean participa = false;

        for (Asaltante participante : caso.getAsalto().getAsaltantes()) {
            if (participante.getId().equals(asaltante.getId())) {
                participa = true;
                break;
            }
        }

        if (!participa) {
            throw new AsaltanteNoParticipaEnAsaltoException();
        }

        if (tiempoCarcel < 0) {
            throw new DatosCondenaInvalidosException();
        }

        if (condenado && tiempoCarcel == 0) {
            throw new CasoCondenadoSinCarcelException();
        }

        if (!condenado && tiempoCarcel != 0) {
            throw new CasoSinCondenaConCarcelException();
        }

        Optional<SentenciaAsaltante> resultado = sentenciaAsaltanteRepository.findByCasoJudicial_IdAndAsaltante_Id(caso.getId(), asaltante.getId());

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("SentenciaAsaltante", asaltanteCodigo);
        }

        SentenciaAsaltante sentencia = resultado.get();

        if (sentencia.isSentenciado()) {
            throw new CasoSentenciadoException();
        }

        sentencia.registrarSentencia(condenado, tiempoCarcel);
        sentenciaAsaltanteRepository.save(sentencia);

        actualizarEstadoSentenciado(caso.getId());
    }

    @Transactional
    public void modificarSentencia(String casoCodigo, String asaltanteCodigo, boolean condenado, int tiempoCarcel) {
        CasoJudicial caso = obtenerCaso(casoCodigo);
        Asaltante asaltante = obtenerAsaltante(asaltanteCodigo);

        if (!caso.isActivo()) {
            throw new OperacionNoPermitidaException("El caso judicial está inactivo");
        }

        if (tiempoCarcel < 0) {
            throw new DatosCondenaInvalidosException();
        }

        if (condenado && tiempoCarcel == 0) {
            throw new CasoCondenadoSinCarcelException();
        }

        if (!condenado && tiempoCarcel != 0) {
            throw new CasoSinCondenaConCarcelException();
        }

        Optional<SentenciaAsaltante> resultado = sentenciaAsaltanteRepository.findByCasoJudicial_IdAndAsaltante_Id(caso.getId(), asaltante.getId());

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("SentenciaAsaltante", asaltanteCodigo);
        }

        SentenciaAsaltante sentencia = resultado.get();

        if (!sentencia.isSentenciado()) {
            throw new OperacionNoPermitidaException("El asaltante todavía no tiene una sentencia registrada");
        }

        sentencia.modificarSentencia(condenado, tiempoCarcel);
        sentenciaAsaltanteRepository.save(sentencia);
    }

    @Transactional(readOnly = true)
    public List<SentenciaAsaltanteDTO> listarSentenciasPorCaso(String casoCodigo) {
        CasoJudicial caso = obtenerCaso(casoCodigo);
        List<SentenciaAsaltante> sentencias = sentenciaAsaltanteRepository.findByCasoJudicial_Id(caso.getId());
        return convertirSentenciasADTO(sentencias);
    }

    @Transactional(readOnly = true)
    public List<SentenciaAsaltanteDTO> listarSentenciasPorAsaltante(String asaltanteCodigo) {
        Asaltante asaltante = obtenerAsaltante(asaltanteCodigo);
        List<SentenciaAsaltante> sentencias = sentenciaAsaltanteRepository.findByAsaltante_Id(asaltante.getId());
        return convertirSentenciasADTO(sentencias);
    }

    private List<SentenciaAsaltanteDTO> convertirSentenciasADTO(List<SentenciaAsaltante> sentencias) {
        List<SentenciaAsaltanteDTO> resultado = new ArrayList<>();

        for (SentenciaAsaltante sentencia : sentencias) {
            SentenciaAsaltanteDTO dto = new SentenciaAsaltanteDTO();
            dto.setAsaltanteCodigo(sentencia.getAsaltante().getCodigo());
            dto.setSentenciado(sentencia.isSentenciado());
            dto.setCondenado(sentencia.isSentenciado() ? sentencia.isCondenado() : null);
            dto.setTiempoCarcel(sentencia.isSentenciado() ? sentencia.getTiempoCarcel() : null);
            resultado.add(dto);
        }

        return resultado;
    }

    @Transactional
    public void actualizarEstadoCaso(Long casoId) {
        Optional<CasoJudicial> resultado = casoJudicialRepository.findById(casoId);

        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("CasoJudicial", String.valueOf(casoId));
        }

        CasoJudicial caso = resultado.get();
        List<SentenciaAsaltante> participaciones = sentenciaAsaltanteRepository.findByCasoJudicial_Id(casoId);

        if (participaciones.isEmpty()) {
            return;
        }

        boolean todosResueltos = true;
        boolean todosSentenciados = true;

        for (SentenciaAsaltante participacion : participaciones) {
            if (participacion.getEstado() == EstadoParticipacionJudicial.PENDIENTE) {
                todosResueltos = false;
            }

            if (!participacion.isSentenciado()) {
                todosSentenciados = false;
            }
        }

        caso.actualizarEstadoSentenciado(todosSentenciados);

        if (todosResueltos) {
            caso.cerrarAutomaticamente();
        }

        casoJudicialRepository.save(caso);
    }
}
