package prog2.policia_backend.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AsaltoServiceTest {

    private AsaltoRepository asaltoRepository;
    private AsaltanteRepository asaltanteRepository;
    private SucursalRepository sucursalRepository;
    private CasoJudicialRepository casoJudicialRepository;
    private SentenciaAsaltanteRepository sentenciaAsaltanteRepository;
    private AsaltoService asaltoService;

    @BeforeEach
    void setUp() {
        asaltoRepository = mock(AsaltoRepository.class);
        asaltanteRepository = mock(AsaltanteRepository.class);
        sucursalRepository = mock(SucursalRepository.class);
        casoJudicialRepository = mock(CasoJudicialRepository.class);
        sentenciaAsaltanteRepository = mock(SentenciaAsaltanteRepository.class);

        asaltoService = new AsaltoService(
                asaltoRepository,
                asaltanteRepository,
                sucursalRepository,
                casoJudicialRepository,
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

    private Sucursal crearSucursal() {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setCodigo("SUC00001");
        sucursal.setActivo(true);
        return sucursal;
    }

    private Asalto crearAsalto() {
        List<Asaltante> asaltantes = new ArrayList<>();
        asaltantes.add(crearAsaltante());

        Asalto asalto = new Asalto(
                LocalDate.of(2026, 10, 1),
                crearSucursal(),
                asaltantes
        );

        asalto.asignarCodigo("AST00001");
        return asalto;
    }

    private AsaltoDTO crearDTO() {
        AsaltoDTO dto = new AsaltoDTO();
        dto.setFecha(LocalDate.of(2026, 10, 1));
        dto.setSucursalCodigo("SUC00001");
        dto.setAsaltantesCodigos(
                new ArrayList<>(Arrays.asList("ASS00001"))
        );
        return dto;
    }

    private void configurarGuardado() {
        when(asaltoRepository.save(any(Asalto.class)))
                .thenAnswer(new Answer<Asalto>() {
                    @Override
                    public Asalto answer(InvocationOnMock invocation) {
                        Asalto asalto = invocation.getArgument(0);
                        return asalto;
                    }
                });
    }

    private void configurarAsaltoExistente(Asalto asalto) {
        when(asaltoRepository.findByCodigo("AST00001"))
                .thenReturn(Optional.of(asalto));

        when(asaltoRepository.save(any(Asalto.class)))
                .thenReturn(asalto);
    }

    // LISTAR
    @Test
    void listarTodosLosAsaltos() {
        List<Asalto> asaltos = new ArrayList<>();
        asaltos.add(crearAsalto());

        when(asaltoRepository.findAll()).thenReturn(asaltos);

        List<AsaltoDTO> resultado = asaltoService.listar();

        assertEquals(1, resultado.size());
        assertEquals("AST00001", resultado.get(0).getCodigo());
        assertEquals("SUC00001", resultado.get(0).getSucursalCodigo());
        assertEquals("ASS00001",
                resultado.get(0).getAsaltantesCodigos().get(0));
    }

    @Test
    void listarAsaltosVacios() {
        when(asaltoRepository.findAll())
                .thenReturn(new ArrayList<Asalto>());

        List<AsaltoDTO> resultado = asaltoService.listar();

        assertTrue(resultado.isEmpty());
    }

    // BUSCAR POR CODIGO
    @Test
    void buscarPorCodigoCorrectamente() {
        when(asaltoRepository.findByCodigo("AST00001"))
                .thenReturn(Optional.of(crearAsalto()));

        AsaltoDTO resultado
                = asaltoService.buscarPorCodigo("AST00001");

        assertEquals("AST00001", resultado.getCodigo());
        assertEquals("SUC00001", resultado.getSucursalCodigo());
        assertEquals(1, resultado.getAsaltantesCodigos().size());
    }

    @Test
    void buscarPorCodigoInexistente() {
        when(asaltoRepository.findByCodigo("AST00001"))
                .thenReturn(Optional.empty());

        try {
            asaltoService.buscarPorCodigo("AST00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // LISTAR POR ASALTANTE
    @Test
    void listarPorAsaltanteCorrectamente() {
        Asaltante asaltante = crearAsaltante();

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asaltante));

        when(asaltoRepository.findByAsaltantes_Id(1L))
                .thenReturn(Arrays.asList(crearAsalto()));

        List<AsaltoDTO> resultado
                = asaltoService.listarPorAsaltante("ASS00001");

        assertEquals(1, resultado.size());
        assertEquals("AST00001", resultado.get(0).getCodigo());

        verify(asaltoRepository).findByAsaltantes_Id(1L);
    }

    @Test
    void listarPorAsaltanteInexistente() {
        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.empty());

        try {
            asaltoService.listarPorAsaltante("ASS00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }

        verifyNoInteractions(asaltoRepository);
    }

    // LISTAR POR SUCURSAL
    @Test
    void listarPorSucursalCorrectamente() {
        Sucursal sucursal = crearSucursal();

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(sucursal));

        when(asaltoRepository.findBySucursal_Id(1L))
                .thenReturn(Arrays.asList(crearAsalto()));

        List<AsaltoDTO> resultado
                = asaltoService.listarPorSucursal("SUC00001");

        assertEquals(1, resultado.size());
        assertEquals("AST00001", resultado.get(0).getCodigo());

        verify(asaltoRepository).findBySucursal_Id(1L);
    }

    @Test
    void listarPorSucursalInexistente() {
        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.empty());

        try {
            asaltoService.listarPorSucursal("SUC00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }

        verifyNoInteractions(asaltoRepository);
    }

    // GUARDAR
    @Test
    void guardarAsaltoCorrectamente() {
        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(crearAsaltante()));

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(crearSucursal()));

        when(asaltoRepository.save(any(Asalto.class)))
                .thenAnswer(new Answer<Asalto>() {
                    @Override
                    public Asalto answer(InvocationOnMock invocation) {
                        Asalto asalto = invocation.getArgument(0);

                        if (asalto.getId() == null) {
                            org.springframework.test.util.ReflectionTestUtils
                                    .setField(asalto, "id", 1L);
                        }

                        return asalto;
                    }
                });

        AsaltoDTO resultado = asaltoService.guardar(crearDTO());

        assertNotNull(resultado);
        assertEquals("AST00001", resultado.getCodigo());
        assertEquals("SUC00001", resultado.getSucursalCodigo());
        assertEquals(1, resultado.getAsaltantesCodigos().size());

        verify(asaltoRepository, times(2))
                .save(any(Asalto.class));
    }

    @Test
    void guardarAsaltoSinAsaltantes() {
        AsaltoDTO dto = crearDTO();
        dto.setAsaltantesCodigos(null);

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(crearSucursal()));

        when(asaltoRepository.save(any(Asalto.class)))
                .thenAnswer(new Answer<Asalto>() {
                    @Override
                    public Asalto answer(InvocationOnMock invocation) {
                        Asalto asalto = invocation.getArgument(0);

                        if (asalto.getId() == null) {
                            org.springframework.test.util.ReflectionTestUtils
                                    .setField(asalto, "id", 1L);
                        }

                        return asalto;
                    }
                });

        AsaltoDTO resultado = asaltoService.guardar(dto);

        assertTrue(resultado.getAsaltantesCodigos().isEmpty());
        verifyNoInteractions(asaltanteRepository);
    }

    @Test
    void guardarRechazaAsaltanteDuplicado() {
        AsaltoDTO dto = crearDTO();
        dto.setAsaltantesCodigos(
                Arrays.asList("ASS00001", "ASS00001")
        );

        try {
            asaltoService.guardar(dto);
            fail("Debía lanzar AsaltanteDuplicadoException");
        } catch (AsaltanteDuplicadoException e) {
            assertNotNull(e);
        }

        verify(asaltoRepository, never())
                .save(any(Asalto.class));
    }

    @Test
    void guardarRechazaAsaltanteInexistente() {
        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.empty());

        try {
            asaltoService.guardar(crearDTO());
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }

        verify(asaltoRepository, never())
                .save(any(Asalto.class));
    }

    @Test
    void guardarRechazaAsaltanteInactivo() {
        Asaltante asaltante = crearAsaltante();
        asaltante.setActivo(false);

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asaltante));

        try {
            asaltoService.guardar(crearDTO());
            fail("Debía lanzar PersonaInactivaException");
        } catch (PersonaInactivaException e) {
            assertNotNull(e);
        }

        verify(asaltoRepository, never())
                .save(any(Asalto.class));
    }

    @Test
    void guardarRechazaSucursalInexistente() {
        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(crearAsaltante()));

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.empty());

        try {
            asaltoService.guardar(crearDTO());
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }

        verify(asaltoRepository, never())
                .save(any(Asalto.class));
    }

    @Test
    void guardarRechazaSucursalCerrada() {
        Sucursal sucursal = crearSucursal();
        sucursal.setActivo(false);

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(crearAsaltante()));

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(sucursal));

        try {
            asaltoService.guardar(crearDTO());
            fail("Debía lanzar SucursalYaCerradaException");
        } catch (SucursalYaCerradaException e) {
            assertNotNull(e);
        }

        verify(asaltoRepository, never())
                .save(any(Asalto.class));
    }

    // ACTUALIZAR
    @Test
    void actualizarSucursalCorrectamente() {
        Asalto asalto = crearAsalto();
        configurarAsaltoExistente(asalto);

        Sucursal nuevaSucursal = crearSucursal();
        nuevaSucursal.setId(2L);
        nuevaSucursal.setCodigo("SUC00002");

        when(sucursalRepository.findByCodigo("SUC00002"))
                .thenReturn(Optional.of(nuevaSucursal));

        AsaltoDTO dto = new AsaltoDTO();
        dto.setSucursalCodigo("SUC00002");

        AsaltoDTO resultado
                = asaltoService.actualizar("AST00001", dto);

        assertEquals("SUC00002", resultado.getSucursalCodigo());
        verify(asaltoRepository).save(asalto);
    }

    @Test
    void actualizarAgregaAsaltante() {
        Asalto asalto = crearAsalto();
        configurarAsaltoExistente(asalto);

        Asaltante nuevo = crearAsaltante();
        nuevo.setId(2L);
        nuevo.setCodigo("ASS00002");

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asalto.getAsaltantes().get(0)));

        when(asaltanteRepository.findByCodigo("ASS00002"))
                .thenReturn(Optional.of(nuevo));

        AsaltoDTO dto = new AsaltoDTO();
        dto.setAsaltantesCodigos(
                Arrays.asList("ASS00001", "ASS00002")
        );

        AsaltoDTO resultado
                = asaltoService.actualizar("AST00001", dto);

        assertEquals(2, resultado.getAsaltantesCodigos().size());
        assertTrue(resultado.getAsaltantesCodigos().contains("ASS00002"));

        verify(asaltoRepository).save(asalto);
    }

    @Test
    void actualizarQuitaAsaltante() {
        Asalto asalto = crearAsalto();
        configurarAsaltoExistente(asalto);

        AsaltoDTO dto = new AsaltoDTO();
        dto.setAsaltantesCodigos(new ArrayList<String>());

        AsaltoDTO resultado
                = asaltoService.actualizar("AST00001", dto);

        assertTrue(resultado.getAsaltantesCodigos().isEmpty());
        assertTrue(asalto.getAsaltantes().isEmpty());

        verify(asaltoRepository).save(asalto);
    }

    @Test
    void actualizarSinCambios() {
        Asalto asalto = crearAsalto();
        configurarAsaltoExistente(asalto);

        AsaltoDTO dto = new AsaltoDTO();
        dto.setAsaltantesCodigos(null);

        AsaltoDTO resultado
                = asaltoService.actualizar("AST00001", dto);

        assertEquals("AST00001", resultado.getCodigo());
        assertEquals("SUC00001", resultado.getSucursalCodigo());
        assertEquals(1, resultado.getAsaltantesCodigos().size());

        verify(asaltoRepository).save(asalto);
    }

    @Test
    void actualizarAsaltoInexistente() {
        when(asaltoRepository.findByCodigo("AST00001"))
                .thenReturn(Optional.empty());

        try {
            asaltoService.actualizar("AST00001", new AsaltoDTO());
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }

        verify(asaltoRepository, never())
                .save(any(Asalto.class));
    }

    @Test
    void actualizarRechazaSucursalCerrada() {
        Asalto asalto = crearAsalto();
        configurarAsaltoExistente(asalto);

        Sucursal sucursal = crearSucursal();
        sucursal.setActivo(false);

        when(sucursalRepository.findByCodigo("SUC00002"))
                .thenReturn(Optional.of(sucursal));

        AsaltoDTO dto = new AsaltoDTO();
        dto.setSucursalCodigo("SUC00002");

        try {
            asaltoService.actualizar("AST00001", dto);
            fail("Debía lanzar SucursalYaCerradaException");
        } catch (SucursalYaCerradaException e) {
            assertNotNull(e);
        }

        verify(asaltoRepository, never())
                .save(any(Asalto.class));
    }

    @Test
    void actualizarRechazaAsaltanteDuplicado() {
        Asalto asalto = crearAsalto();
        configurarAsaltoExistente(asalto);

        AsaltoDTO dto = new AsaltoDTO();
        dto.setAsaltantesCodigos(
                Arrays.asList("ASS00001", "ASS00001")
        );

        try {
            asaltoService.actualizar("AST00001", dto);
            fail("Debía lanzar AsaltanteDuplicadoException");
        } catch (AsaltanteDuplicadoException e) {
            assertNotNull(e);
        }

        verify(asaltoRepository, never())
                .save(any(Asalto.class));
    }

    @Test
    void actualizarRechazaQuitarAsaltanteConCasoJudicial() {
        Asalto asalto = crearAsalto();
        CasoJudicial caso = mock(CasoJudicial.class);

        org.springframework.test.util.ReflectionTestUtils
                .setField(asalto, "casoJudicial", caso);

        configurarAsaltoExistente(asalto);

        AsaltoDTO dto = new AsaltoDTO();
        dto.setAsaltantesCodigos(new ArrayList<String>());

        try {
            asaltoService.actualizar("AST00001", dto);
            fail("Debía lanzar AsaltanteConCasoJudicialException");
        } catch (AsaltanteConCasoJudicialException e) {
            assertNotNull(e);
        }

        assertEquals(1, asalto.getAsaltantes().size());

        verify(asaltoRepository, never())
                .save(any(Asalto.class));
    }

    @Test
    void actualizarRechazaCambiarSucursalConCasoJudicial() {
        Asalto asalto = crearAsalto();
        CasoJudicial caso = mock(CasoJudicial.class);

        org.springframework.test.util.ReflectionTestUtils
                .setField(asalto, "casoJudicial", caso);

        configurarAsaltoExistente(asalto);

        Sucursal nuevaSucursal = crearSucursal();
        nuevaSucursal.setId(2L);
        nuevaSucursal.setCodigo("SUC00002");

        when(sucursalRepository.findByCodigo("SUC00002"))
                .thenReturn(Optional.of(nuevaSucursal));

        AsaltoDTO dto = new AsaltoDTO();
        dto.setSucursalCodigo("SUC00002");

        try {
            asaltoService.actualizar("AST00001", dto);
            fail("Debía lanzar OperacionNoPermitidaException");
        } catch (OperacionNoPermitidaException e) {
            assertNotNull(e);
        }

        assertEquals("SUC00001", asalto.getSucursal().getCodigo());

        verify(asaltoRepository, never())
                .save(any(Asalto.class));
    }

    @Test
    void actualizarAgregaAsaltanteConCasoJudicial() {
        Asalto asalto = crearAsalto();
        CasoJudicial caso = mock(CasoJudicial.class);

        org.springframework.test.util.ReflectionTestUtils
                .setField(asalto, "casoJudicial", caso);

        configurarAsaltoExistente(asalto);

        Asaltante nuevo = crearAsaltante();
        nuevo.setId(2L);
        nuevo.setCodigo("ASS00002");

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asalto.getAsaltantes().get(0)));

        when(asaltanteRepository.findByCodigo("ASS00002"))
                .thenReturn(Optional.of(nuevo));

        AsaltoDTO dto = new AsaltoDTO();
        dto.setAsaltantesCodigos(
                Arrays.asList("ASS00001", "ASS00002")
        );

        AsaltoDTO resultado
                = asaltoService.actualizar("AST00001", dto);

        assertEquals(2, resultado.getAsaltantesCodigos().size());

        verify(sentenciaAsaltanteRepository)
                .save(any(SentenciaAsaltante.class));

        verify(caso).actualizarEstadoSentenciado(false);
        verify(casoJudicialRepository).save(caso);
        verify(asaltoRepository).save(asalto);
    }
}
