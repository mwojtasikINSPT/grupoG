package prog2.policia_backend.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
import prog2.policia_backend.models.Juez;
import prog2.policia_backend.models.SentenciaAsaltante;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.AsaltoRepository;
import prog2.policia_backend.repositories.CasoJudicialRepository;
import prog2.policia_backend.repositories.JuezRepository;
import prog2.policia_backend.repositories.SentenciaAsaltanteRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CasoJudicialServiceTest {

    private CasoJudicialRepository casoJudicialRepository;
    private AsaltoRepository asaltoRepository;
    private JuezRepository juezRepository;
    private AsaltanteRepository asaltanteRepository;
    private SentenciaAsaltanteRepository sentenciaAsaltanteRepository;
    private CasoJudicialService casoJudicialService;

    @BeforeEach
    void setUp() {
        casoJudicialRepository = mock(CasoJudicialRepository.class);
        asaltoRepository = mock(AsaltoRepository.class);
        juezRepository = mock(JuezRepository.class);
        asaltanteRepository = mock(AsaltanteRepository.class);
        sentenciaAsaltanteRepository = mock(SentenciaAsaltanteRepository.class);

        casoJudicialService = new CasoJudicialService(
                casoJudicialRepository,
                asaltoRepository,
                juezRepository,
                asaltanteRepository,
                sentenciaAsaltanteRepository
        );
    }

    private Asaltante crearAsaltante() {
        Asaltante asaltante = new Asaltante();
        asaltante.setId(1L);
        asaltante.setCodigo("ASS00001");
        asaltante.setActivo(true);
        return asaltante;
    }

    private Juez crearJuez() {
        Juez juez = mock(Juez.class);

        when(juez.getId()).thenReturn(1L);
        when(juez.getCodigo()).thenReturn("JUE00001");
        when(juez.isActivo()).thenReturn(true);

        return juez;
    }

    private Asalto crearAsalto() {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setCodigo("SUC00001");

        List<Asaltante> asaltantes = new ArrayList<>();
        asaltantes.add(crearAsaltante());

        Asalto asalto = new Asalto(
                LocalDate.of(2026, 10, 1),
                sucursal,
                asaltantes
        );

        ReflectionTestUtils.setField(asalto, "id", 1L);
        asalto.asignarCodigo("AST00001");

        return asalto;
    }

    private CasoJudicial crearCaso() {
        CasoJudicial caso = new CasoJudicial(
                "CJU00001",
                crearAsalto(),
                crearJuez()
        );

        ReflectionTestUtils.setField(caso, "id", 1L);
        return caso;
    }

    private CasoJudicialDTO crearDTO() {
        CasoJudicialDTO dto = new CasoJudicialDTO();
        dto.setAsaltoCodigo("AST00001");
        dto.setJuezCodigo("JUE00001");
        return dto;
    }

    private void configurarCasoExistente(CasoJudicial caso) {
        when(casoJudicialRepository.findByCodigo("CJU00001"))
                .thenReturn(Optional.of(caso));

        when(casoJudicialRepository.save(any(CasoJudicial.class)))
                .thenReturn(caso);
    }

    private void configurarGuardado() {
        when(casoJudicialRepository.saveAndFlush(any(CasoJudicial.class)))
                .thenAnswer(new org.mockito.stubbing.Answer<CasoJudicial>() {
                    @Override
                    public CasoJudicial answer(
                            org.mockito.invocation.InvocationOnMock invocation) {

                        CasoJudicial caso = invocation.getArgument(0);
                        ReflectionTestUtils.setField(caso, "id", 1L);
                        return caso;
                    }
                });

        when(casoJudicialRepository.save(any(CasoJudicial.class)))
                .thenAnswer(new org.mockito.stubbing.Answer<CasoJudicial>() {
                    @Override
                    public CasoJudicial answer(
                            org.mockito.invocation.InvocationOnMock invocation) {

                        return invocation.getArgument(0);
                    }
                });
    }

    private void configurarDatosCreacion(Asalto asalto, Juez juez) {
        when(asaltoRepository.findByCodigo("AST00001"))
                .thenReturn(Optional.of(asalto));

        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.of(juez));
    }

    // LISTAR
    @Test
    void listarTodosLosCasos() {
        List<CasoJudicial> casos = new ArrayList<>();
        casos.add(crearCaso());

        when(casoJudicialRepository.findAll()).thenReturn(casos);

        List<CasoJudicialDTO> resultado = casoJudicialService.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("CJU00001", resultado.get(0).getCodigo());
        assertEquals("AST00001", resultado.get(0).getAsaltoCodigo());
        assertEquals("JUE00001", resultado.get(0).getJuezCodigo());
        assertTrue(resultado.get(0).getActivo());
    }

    @Test
    void listarCasosActivos() {
        CasoJudicial caso = crearCaso();

        when(casoJudicialRepository.findByActivo(true))
                .thenReturn(Arrays.asList(caso));

        List<CasoJudicialDTO> resultado = casoJudicialService.listar(true);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());

        verify(casoJudicialRepository).findByActivo(true);
    }

    @Test
    void listarCasosInactivos() {
        when(casoJudicialRepository.findByActivo(false))
                .thenReturn(new ArrayList<CasoJudicial>());

        List<CasoJudicialDTO> resultado = casoJudicialService.listar(false);

        assertTrue(resultado.isEmpty());
    }

    // BUSCAR POR CODIGO
    @Test
    void buscarPorCodigoCorrectamente() {
        configurarCasoExistente(crearCaso());

        CasoJudicialDTO resultado
                = casoJudicialService.buscarPorCodigo("CJU00001");

        assertEquals("CJU00001", resultado.getCodigo());
        assertEquals("AST00001", resultado.getAsaltoCodigo());
        assertEquals("JUE00001", resultado.getJuezCodigo());
        assertEquals("ASS00001", resultado.getAsaltanteCodigos().get(0));
    }

    @Test
    void buscarPorCodigoInexistente() {
        when(casoJudicialRepository.findByCodigo("CJU00001"))
                .thenReturn(Optional.empty());

        try {
            casoJudicialService.buscarPorCodigo("CJU00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // GUARDAR
    @Test
    void guardarCasoCorrectamente() {
        configurarDatosCreacion(crearAsalto(), crearJuez());
        configurarGuardado();

        CasoJudicialDTO resultado
                = casoJudicialService.guardar(crearDTO());

        assertNotNull(resultado);
        assertEquals("CJU00001", resultado.getCodigo());
        assertEquals("AST00001", resultado.getAsaltoCodigo());
        assertEquals("JUE00001", resultado.getJuezCodigo());
        assertTrue(resultado.getActivo());
        assertFalse(resultado.getSentenciado());

        verify(casoJudicialRepository).saveAndFlush(any(CasoJudicial.class));
        verify(casoJudicialRepository).save(any(CasoJudicial.class));
        verify(sentenciaAsaltanteRepository).saveAll(anyList());
    }

    @Test
    void guardarRechazaCasoYaExistente() {
        configurarDatosCreacion(crearAsalto(), crearJuez());

        when(casoJudicialRepository.existsByAsalto_Id(1L))
                .thenReturn(true);

        try {
            casoJudicialService.guardar(crearDTO());
            fail("Debía lanzar CasoJudicialYaExistenteException");
        } catch (CasoJudicialYaExistenteException e) {
            assertNotNull(e);
        }

        verify(casoJudicialRepository, never())
                .saveAndFlush(any(CasoJudicial.class));
    }

    @Test
    void guardarRechazaAsaltoSinAsaltantes() {
        Asalto asalto = crearAsalto();
        asalto.getAsaltantes().clear();

        configurarDatosCreacion(asalto, crearJuez());

        try {
            casoJudicialService.guardar(crearDTO());
            fail("Debía lanzar OperacionNoPermitidaException");
        } catch (OperacionNoPermitidaException e) {
            assertNotNull(e);
        }

        verify(casoJudicialRepository, never())
                .saveAndFlush(any(CasoJudicial.class));
    }

    @Test
    void guardarRechazaAsaltanteInactivo() {
        Asalto asalto = crearAsalto();
        asalto.getAsaltantes().get(0).setActivo(false);

        configurarDatosCreacion(asalto, crearJuez());

        try {
            casoJudicialService.guardar(crearDTO());
            fail("Debía lanzar PersonaInactivaException");
        } catch (PersonaInactivaException e) {
            assertNotNull(e);
        }

        verify(casoJudicialRepository, never())
                .saveAndFlush(any(CasoJudicial.class));
    }

    @Test
    void guardarRechazaJuezInactivo() {
        Juez juez = crearJuez();
        when(juez.isActivo()).thenReturn(false);

        configurarDatosCreacion(crearAsalto(), juez);

        try {
            casoJudicialService.guardar(crearDTO());
            fail("Debía lanzar PersonaInactivaException");
        } catch (PersonaInactivaException e) {
            assertNotNull(e);
        }

        verify(casoJudicialRepository, never())
                .saveAndFlush(any(CasoJudicial.class));
    }

    @Test
    void guardarRechazaAsaltoInexistente() {
        when(asaltoRepository.findByCodigo("AST00001"))
                .thenReturn(Optional.empty());

        try {
            casoJudicialService.guardar(crearDTO());
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }

        verify(casoJudicialRepository, never())
                .saveAndFlush(any(CasoJudicial.class));
    }

    // ACTUALIZAR
    @Test
    void actualizarJuezCorrectamente() {
        CasoJudicial caso = crearCaso();
        configurarCasoExistente(caso);

        Juez nuevoJuez = mock(Juez.class);
        when(nuevoJuez.isActivo()).thenReturn(true);
        when(nuevoJuez.getCodigo()).thenReturn("JUE00002");

        when(juezRepository.findByCodigo("JUE00002"))
                .thenReturn(Optional.of(nuevoJuez));

        CasoJudicialDTO dto = new CasoJudicialDTO();
        dto.setJuezCodigo("JUE00002");

        CasoJudicialDTO resultado
                = casoJudicialService.actualizar("CJU00001", dto);

        assertEquals("JUE00002", resultado.getJuezCodigo());
        verify(casoJudicialRepository).save(caso);
    }

    @Test
    void actualizarCasoInactivo() {
        CasoJudicial caso = crearCaso();
        caso.actualizarActivo(false);
        configurarCasoExistente(caso);

        try {
            casoJudicialService.actualizar("CJU00001", new CasoJudicialDTO());
            fail("Debía lanzar OperacionNoPermitidaException");
        } catch (OperacionNoPermitidaException e) {
            assertNotNull(e);
        }

        verify(casoJudicialRepository, never())
                .save(any(CasoJudicial.class));
    }

    @Test
    void actualizarSinCambios() {
        CasoJudicial caso = crearCaso();
        configurarCasoExistente(caso);

        CasoJudicialDTO resultado
                = casoJudicialService.actualizar("CJU00001", new CasoJudicialDTO());

        assertEquals("JUE00001", resultado.getJuezCodigo());
        verify(casoJudicialRepository).save(caso);
    }

    // LISTAR POR ASALTANTE
    @Test
    void listarPorAsaltanteCorrectamente() {
        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(crearAsaltante()));

        CasoJudicial caso = crearCaso();

        when(casoJudicialRepository.findBySentencias_Asaltante_Id(1L))
                .thenReturn(Arrays.asList(caso));

        List<CasoJudicialDTO> resultado
                = casoJudicialService.listarPorAsaltante("ASS00001");

        assertEquals(1, resultado.size());
        assertEquals("CJU00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarPorAsaltanteInexistente() {
        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.empty());

        try {
            casoJudicialService.listarPorAsaltante("ASS00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // LISTAR POR JUEZ
    @Test
    void listarPorJuezCorrectamente() {
        Juez juez = crearJuez();
        CasoJudicial caso = crearCaso();

        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.of(juez));

        when(casoJudicialRepository.findByJuez_Id(1L))
                .thenReturn(Arrays.asList(caso));

        List<CasoJudicialDTO> resultado
                = casoJudicialService.listarPorJuez("JUE00001");

        assertEquals(1, resultado.size());
        assertEquals("CJU00001", resultado.get(0).getCodigo());
    }

    // ELIMINAR
    @Test
    void eliminarCasoCorrectamente() {
        CasoJudicial caso = crearCaso();
        configurarCasoExistente(caso);

        casoJudicialService.eliminar("CJU00001");

        assertFalse(caso.isActivo());
        verify(casoJudicialRepository).save(caso);
    }

    @Test
    void eliminarCasoYaInactivo() {
        CasoJudicial caso = crearCaso();
        caso.actualizarActivo(false);
        configurarCasoExistente(caso);

        try {
            casoJudicialService.eliminar("CJU00001");
            fail("Debía lanzar OperacionNoPermitidaException");
        } catch (OperacionNoPermitidaException e) {
            assertNotNull(e);
        }

        verify(casoJudicialRepository, never())
                .save(any(CasoJudicial.class));
    }

    // REACTIVAR
    @Test
    void reactivarCasoCorrectamente() {
        CasoJudicial caso = crearCaso();
        caso.actualizarActivo(false);
        configurarCasoExistente(caso);

        CasoJudicialDTO resultado
                = casoJudicialService.reactivar("CJU00001");

        assertTrue(caso.isActivo());
        assertTrue(resultado.getActivo());

        verify(casoJudicialRepository).save(caso);
    }

    @Test
    void reactivarCasoYaActivo() {
        CasoJudicial caso = crearCaso();
        configurarCasoExistente(caso);

        try {
            casoJudicialService.reactivar("CJU00001");
            fail("Debía lanzar CasoJudicialActivoException");
        } catch (CasoJudicialActivoException e) {
            assertNotNull(e);
        }

        verify(casoJudicialRepository, never())
                .save(any(CasoJudicial.class));
    }

    // ACTUALIZAR ESTADO SENTENCIADO
    @Test
    void actualizarEstadoSentenciadoCompleto() {
        CasoJudicial caso = crearCaso();
        SentenciaAsaltante sentencia = mock(SentenciaAsaltante.class);

        when(sentencia.getAsaltante())
                .thenReturn(caso.getAsalto().getAsaltantes().get(0));

        when(sentencia.isSentenciado()).thenReturn(true);

        when(casoJudicialRepository.findById(1L))
                .thenReturn(Optional.of(caso));

        when(sentenciaAsaltanteRepository.findByCasoJudicial_Id(1L))
                .thenReturn(Arrays.asList(sentencia));

        casoJudicialService.actualizarEstadoSentenciado(1L);

        assertTrue(caso.isSentenciado());
        verify(casoJudicialRepository).save(caso);
    }

    @Test
    void actualizarEstadoSentenciadoIncompleto() {
        CasoJudicial caso = crearCaso();

        when(casoJudicialRepository.findById(1L))
                .thenReturn(Optional.of(caso));

        when(sentenciaAsaltanteRepository.findByCasoJudicial_Id(1L))
                .thenReturn(new ArrayList<SentenciaAsaltante>());

        casoJudicialService.actualizarEstadoSentenciado(1L);

        assertFalse(caso.isSentenciado());
        verify(casoJudicialRepository).save(caso);
    }

    @Test
    void actualizarEstadoCasoInexistente() {
        when(casoJudicialRepository.findById(1L))
                .thenReturn(Optional.empty());

        try {
            casoJudicialService.actualizarEstadoSentenciado(1L);
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // REGISTRAR SENTENCIA
    @Test
    void registrarSentenciaCorrectamente() {
        CasoJudicial caso = crearCaso();
        Asaltante asaltante = caso.getAsalto().getAsaltantes().get(0);
        SentenciaAsaltante sentencia = mock(SentenciaAsaltante.class);

        configurarCasoExistente(caso);

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asaltante));

        when(sentenciaAsaltanteRepository
                .findByCasoJudicial_IdAndAsaltante_Id(1L, 1L))
                .thenReturn(Optional.of(sentencia));

        when(casoJudicialRepository.findById(1L))
                .thenReturn(Optional.of(caso));

        when(sentenciaAsaltanteRepository.findByCasoJudicial_Id(1L))
                .thenReturn(Arrays.asList(sentencia));

        when(sentencia.getAsaltante()).thenReturn(asaltante);
        when(sentencia.isSentenciado()).thenReturn(false, true);

        casoJudicialService.registrarSentencia(
                "CJU00001", "ASS00001", true, 5
        );

        verify(sentencia).registrarSentencia(true, 5);
        verify(sentenciaAsaltanteRepository).save(sentencia);
        verify(casoJudicialRepository).save(caso);
    }

    @Test
    void registrarSentenciaRechazaNoParticipante() {
        CasoJudicial caso = crearCaso();
        Asaltante asaltante = crearAsaltante();
        asaltante.setId(2L);

        configurarCasoExistente(caso);

        when(asaltanteRepository.findByCodigo("ASS00002"))
                .thenReturn(Optional.of(asaltante));

        try {
            casoJudicialService.registrarSentencia(
                    "CJU00001", "ASS00002", true, 5
            );
            fail("Debía lanzar AsaltanteNoParticipaEnAsaltoException");
        } catch (AsaltanteNoParticipaEnAsaltoException e) {
            assertNotNull(e);
        }

        verify(sentenciaAsaltanteRepository, never())
                .save(any(SentenciaAsaltante.class));
    }

    @Test
    void registrarSentenciaRechazaTiempoNegativo() {
        CasoJudicial caso = crearCaso();
        configurarCasoExistente(caso);

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(caso.getAsalto().getAsaltantes().get(0)));

        try {
            casoJudicialService.registrarSentencia(
                    "CJU00001", "ASS00001", true, -1
            );
            fail("Debía lanzar DatosCondenaInvalidosException");
        } catch (DatosCondenaInvalidosException e) {
            assertNotNull(e);
        }
    }

    @Test
    void registrarSentenciaRechazaCondenaSinCarcel() {
        CasoJudicial caso = crearCaso();
        configurarCasoExistente(caso);

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(caso.getAsalto().getAsaltantes().get(0)));

        try {
            casoJudicialService.registrarSentencia(
                    "CJU00001", "ASS00001", true, 0
            );
            fail("Debía lanzar CasoCondenadoSinCarcelException");
        } catch (CasoCondenadoSinCarcelException e) {
            assertNotNull(e);
        }
    }

    @Test
    void registrarSentenciaRechazaSinCondenaConCarcel() {
        CasoJudicial caso = crearCaso();
        configurarCasoExistente(caso);

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(caso.getAsalto().getAsaltantes().get(0)));

        try {
            casoJudicialService.registrarSentencia(
                    "CJU00001", "ASS00001", false, 5
            );
            fail("Debía lanzar CasoSinCondenaConCarcelException");
        } catch (CasoSinCondenaConCarcelException e) {
            assertNotNull(e);
        }
    }

    @Test
    void registrarSentenciaYaRegistrada() {
        CasoJudicial caso = crearCaso();
        configurarCasoExistente(caso);

        Asaltante asaltante = caso.getAsalto().getAsaltantes().get(0);
        SentenciaAsaltante sentencia = mock(SentenciaAsaltante.class);

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asaltante));

        when(sentenciaAsaltanteRepository
                .findByCasoJudicial_IdAndAsaltante_Id(1L, 1L))
                .thenReturn(Optional.of(sentencia));

        when(sentencia.isSentenciado()).thenReturn(true);

        try {
            casoJudicialService.registrarSentencia(
                    "CJU00001", "ASS00001", true, 5
            );
            fail("Debía lanzar CasoSentenciadoException");
        } catch (CasoSentenciadoException e) {
            assertNotNull(e);
        }

        verify(sentenciaAsaltanteRepository, never())
                .save(any(SentenciaAsaltante.class));
    }

    // MODIFICAR SENTENCIA
    @Test
    void modificarSentenciaCorrectamente() {
        CasoJudicial caso = crearCaso();
        configurarCasoExistente(caso);

        Asaltante asaltante = caso.getAsalto().getAsaltantes().get(0);
        SentenciaAsaltante sentencia = mock(SentenciaAsaltante.class);

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asaltante));

        when(sentenciaAsaltanteRepository
                .findByCasoJudicial_IdAndAsaltante_Id(1L, 1L))
                .thenReturn(Optional.of(sentencia));

        when(sentencia.isSentenciado()).thenReturn(true);

        casoJudicialService.modificarSentencia(
                "CJU00001", "ASS00001", true, 10
        );

        verify(sentencia).modificarSentencia(true, 10);
        verify(sentenciaAsaltanteRepository).save(sentencia);
    }

    @Test
    void modificarSentenciaNoRegistrada() {
        CasoJudicial caso = crearCaso();
        configurarCasoExistente(caso);

        Asaltante asaltante = caso.getAsalto().getAsaltantes().get(0);
        SentenciaAsaltante sentencia = mock(SentenciaAsaltante.class);

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asaltante));

        when(sentenciaAsaltanteRepository
                .findByCasoJudicial_IdAndAsaltante_Id(1L, 1L))
                .thenReturn(Optional.of(sentencia));

        when(sentencia.isSentenciado()).thenReturn(false);

        try {
            casoJudicialService.modificarSentencia(
                    "CJU00001", "ASS00001", true, 5
            );
            fail("Debía lanzar OperacionNoPermitidaException");
        } catch (OperacionNoPermitidaException e) {
            assertNotNull(e);
        }

        verify(sentenciaAsaltanteRepository, never())
                .save(any(SentenciaAsaltante.class));
    }

    // LISTAR SENTENCIAS
    @Test
    void listarSentenciasPorCasoCorrectamente() {
        CasoJudicial caso = crearCaso();
        configurarCasoExistente(caso);

        SentenciaAsaltante sentencia = mock(SentenciaAsaltante.class);

        when(sentencia.getAsaltante())
                .thenReturn(caso.getAsalto().getAsaltantes().get(0));

        when(sentencia.isSentenciado()).thenReturn(true);
        when(sentencia.isCondenado()).thenReturn(true);
        when(sentencia.getTiempoCarcel()).thenReturn(5);

        when(sentenciaAsaltanteRepository.findByCasoJudicial_Id(1L))
                .thenReturn(Arrays.asList(sentencia));

        List<SentenciaAsaltanteDTO> resultado
                = casoJudicialService.listarSentenciasPorCaso("CJU00001");

        assertEquals(1, resultado.size());
        assertEquals("ASS00001", resultado.get(0).getAsaltanteCodigo());
        assertTrue(resultado.get(0).isSentenciado());
        assertTrue(resultado.get(0).getCondenado());
        assertEquals(5, resultado.get(0).getTiempoCarcel());
    }

    @Test
    void listarSentenciasPorAsaltanteCorrectamente() {
        Asaltante asaltante = crearAsaltante();
        SentenciaAsaltante sentencia = mock(SentenciaAsaltante.class);

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asaltante));

        when(sentenciaAsaltanteRepository.findByAsaltante_Id(1L))
                .thenReturn(Arrays.asList(sentencia));

        when(sentencia.getAsaltante()).thenReturn(asaltante);
        when(sentencia.isSentenciado()).thenReturn(false);

        List<SentenciaAsaltanteDTO> resultado
                = casoJudicialService.listarSentenciasPorAsaltante("ASS00001");

        assertEquals(1, resultado.size());
        assertEquals("ASS00001", resultado.get(0).getAsaltanteCodigo());
        assertFalse(resultado.get(0).isSentenciado());
        assertNull(resultado.get(0).getCondenado());
        assertNull(resultado.get(0).getTiempoCarcel());
    }
}
