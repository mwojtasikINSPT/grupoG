package prog2.policia_backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import prog2.policia_backend.DTOs.AsaltanteDTO;
import prog2.policia_backend.exceptions.BandaInactivaException;
import prog2.policia_backend.exceptions.MotivoBajaAsaltanteInvalidoException;
import prog2.policia_backend.exceptions.MotivoBajaObligatorioException;
import prog2.policia_backend.exceptions.PersonaInactivaException;
import prog2.policia_backend.exceptions.PersonaYaInactivaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.models.Banda;
import prog2.policia_backend.models.CasoJudicial;
import prog2.policia_backend.models.EstadoParticipacionJudicial;
import prog2.policia_backend.models.SentenciaAsaltante;
import prog2.policia_backend.models.MotivoBajaPersona;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.BandaRepository;
import prog2.policia_backend.repositories.SentenciaAsaltanteRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AsaltanteServiceTest {

    private AsaltanteRepository asaltanteRepository;
    private BandaRepository bandaRepository;
    private AsaltanteService asaltanteService;

    private SentenciaAsaltanteRepository sentenciaAsaltanteRepository;
    private CasoJudicialService casoJudicialService;

    @BeforeEach
    void setUp() {
        asaltanteRepository = mock(AsaltanteRepository.class);
        bandaRepository = mock(BandaRepository.class);
        sentenciaAsaltanteRepository = mock(SentenciaAsaltanteRepository.class);
        casoJudicialService = mock(CasoJudicialService.class);

        asaltanteService = new AsaltanteService(asaltanteRepository, bandaRepository, sentenciaAsaltanteRepository, casoJudicialService);
    }

    private Banda crearBanda() {
        Banda banda = new Banda();

        banda.setId(1L);
        banda.setCodigo("BAN00001");
        banda.setActivo(true);

        return banda;
    }

    private Asaltante crearAsaltante() {
        Asaltante asaltante = new Asaltante();

        asaltante.setId(1L);
        asaltante.setCodigo("ASS00001");
        asaltante.setNombre("Juan Perez");
        asaltante.setBanda(crearBanda());
        asaltante.setActivo(true);

        return asaltante;
    }

    private AsaltanteDTO crearDTO() {
        AsaltanteDTO dto = new AsaltanteDTO();

        dto.setNombre("Juan Perez");

        return dto;
    }

    private void configurarGuardado() {
        when(asaltanteRepository.save(any(Asaltante.class)))
                .thenAnswer(new org.mockito.stubbing.Answer<Asaltante>() {
                    @Override
                    public Asaltante answer(
                            org.mockito.invocation.InvocationOnMock invocation) {

                        Asaltante asaltante = invocation.getArgument(0);

                        if (asaltante.getId() == null) {
                            asaltante.setId(1L);
                        }

                        return asaltante;
                    }
                });
    }

    // LISTAR
    @Test
    void listarTodosLosAsaltantes() {
        List<Asaltante> asaltantes = new ArrayList<>();
        asaltantes.add(crearAsaltante());

        when(asaltanteRepository.findAll()).thenReturn(asaltantes);

        List<AsaltanteDTO> resultado = asaltanteService.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("ASS00001", resultado.get(0).getCodigo());
        assertEquals("Juan Perez", resultado.get(0).getNombre());
        assertEquals("BAN00001", resultado.get(0).getBandaCodigo());
    }

    @Test
    void listarAsaltantesActivos() {
        List<Asaltante> asaltantes = new ArrayList<>();
        asaltantes.add(crearAsaltante());

        when(asaltanteRepository.findByActivo(true))
                .thenReturn(asaltantes);

        List<AsaltanteDTO> resultado = asaltanteService.listar(true);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());

        verify(asaltanteRepository).findByActivo(true);
    }

    @Test
    void listarAsaltantesInactivos() {
        when(asaltanteRepository.findByActivo(false))
                .thenReturn(new ArrayList<>());

        List<AsaltanteDTO> resultado = asaltanteService.listar(false);

        assertTrue(resultado.isEmpty());
    }

    // BUSCAR POR CODIGO
    @Test
    void buscarPorCodigoCorrectamente() {
        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(crearAsaltante()));

        AsaltanteDTO resultado
                = asaltanteService.buscarPorCodigo("ASS00001");

        assertEquals("ASS00001", resultado.getCodigo());
        assertEquals("Juan Perez", resultado.getNombre());
        assertEquals("BAN00001", resultado.getBandaCodigo());
    }

    @Test
    void buscarPorCodigoInexistente() {
        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.empty());

        try {
            asaltanteService.buscarPorCodigo("ASS00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // BUSCAR POR NOMBRE
    @Test
    void buscarPorNombreCorrectamente() {
        List<Asaltante> asaltantes = new ArrayList<>();
        asaltantes.add(crearAsaltante());

        when(asaltanteRepository.findByNombreContainingIgnoreCase(anyString()))
                .thenReturn(asaltantes);

        List<AsaltanteDTO> resultado
                = asaltanteService.buscarPorNombre("juan", null);

        assertEquals(1, resultado.size());
        assertEquals("Juan Perez", resultado.get(0).getNombre());
    }

    @Test
    void buscarPorNombreRespetaEstado() {
        when(asaltanteRepository.findByNombreContainingIgnoreCaseAndActivo(
                anyString(), eq(false)))
                .thenReturn(new ArrayList<>());

        List<AsaltanteDTO> resultado
                = asaltanteService.buscarPorNombre("Juan", false);

        assertTrue(resultado.isEmpty());

        verify(asaltanteRepository)
                .findByNombreContainingIgnoreCaseAndActivo(anyString(), eq(false));
    }

    @Test
    void buscarPorNombreSinCoincidencias() {
        when(asaltanteRepository.findByNombreContainingIgnoreCase(anyString()))
                .thenReturn(new ArrayList<>());

        List<AsaltanteDTO> resultado
                = asaltanteService.buscarPorNombre("Pedro", null);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorNombreIgnoraMayusculas() {
        List<Asaltante> asaltantes = new ArrayList<>();
        asaltantes.add(crearAsaltante());

        when(asaltanteRepository.findByNombreContainingIgnoreCase(anyString()))
                .thenReturn(asaltantes);

        List<AsaltanteDTO> resultado = asaltanteService.buscarPorNombre("JUAN", null);

        assertEquals(1, resultado.size());
    }

    // GUARDAR
    @Test
    void guardarAsaltanteCorrectamente() {
        configurarGuardado();

        AsaltanteDTO resultado
                = asaltanteService.guardar(crearDTO());

        assertNotNull(resultado);
        assertEquals("ASS00001", resultado.getCodigo());
        assertEquals("Juan Perez", resultado.getNombre());
        assertTrue(resultado.getActivo());
        assertNull(resultado.getBandaCodigo());

        verify(asaltanteRepository, times(2))
                .save(any(Asaltante.class));
    }

    @Test
    void guardarAsaltanteConBandaActiva() {
        Banda banda = crearBanda();

        when(bandaRepository.findByCodigo("BAN00001"))
                .thenReturn(Optional.of(banda));

        configurarGuardado();

        AsaltanteDTO dto = crearDTO();
        dto.setBandaCodigo("BAN00001");

        AsaltanteDTO resultado = asaltanteService.guardar(dto);

        assertEquals("BAN00001", resultado.getBandaCodigo());
        verify(bandaRepository).findByCodigo("BAN00001");
    }

    @Test
    void guardarAsaltanteSinBanda() {
        configurarGuardado();

        AsaltanteDTO dto = crearDTO();
        dto.setBandaCodigo(null);

        AsaltanteDTO resultado = asaltanteService.guardar(dto);

        assertNull(resultado.getBandaCodigo());
        verifyNoInteractions(bandaRepository);
    }

    @Test
    void guardarRechazaBandaInexistente() {
        when(bandaRepository.findByCodigo("BAN00001"))
                .thenReturn(Optional.empty());

        AsaltanteDTO dto = crearDTO();
        dto.setBandaCodigo("BAN00001");

        try {
            asaltanteService.guardar(dto);
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }

        verify(asaltanteRepository, never())
                .save(any(Asaltante.class));
    }

    @Test
    void guardarRechazaBandaInactiva() {
        Banda banda = crearBanda();
        banda.setActivo(false);

        when(bandaRepository.findByCodigo("BAN00001"))
                .thenReturn(Optional.of(banda));

        AsaltanteDTO dto = crearDTO();
        dto.setBandaCodigo("BAN00001");

        try {
            asaltanteService.guardar(dto);
            fail("Debía lanzar BandaInactivaException");
        } catch (BandaInactivaException e) {
            assertNotNull(e);
        }

        verify(asaltanteRepository, never())
                .save(any(Asaltante.class));
    }

    // ACTUALIZAR
    @Test
    void actualizarNombreCorrectamente() {
        Asaltante asaltante = crearAsaltante();

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asaltante));

        when(asaltanteRepository.save(any(Asaltante.class)))
                .thenReturn(asaltante);

        AsaltanteDTO dto = new AsaltanteDTO();
        dto.setNombre("Carlos Gomez");

        AsaltanteDTO resultado
                = asaltanteService.actualizar("ASS00001", dto);

        assertEquals("Carlos Gomez", resultado.getNombre());
        verify(asaltanteRepository).save(asaltante);
    }

    @Test
    void actualizarAsignaBandaCorrectamente() {
        Asaltante asaltante = crearAsaltante();
        Banda nuevaBanda = crearBanda();

        nuevaBanda.setId(2L);
        nuevaBanda.setCodigo("BAN00002");

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asaltante));

        when(bandaRepository.findByCodigo("BAN00002"))
                .thenReturn(Optional.of(nuevaBanda));

        when(asaltanteRepository.save(any(Asaltante.class)))
                .thenReturn(asaltante);

        AsaltanteDTO dto = new AsaltanteDTO();
        dto.setBandaCodigo("BAN00002");

        AsaltanteDTO resultado
                = asaltanteService.actualizar("ASS00001", dto);

        assertEquals("BAN00002", resultado.getBandaCodigo());
        verify(bandaRepository).findByCodigo("BAN00002");
    }

    @Test
    void actualizarQuitaAsaltanteDeBanda() {
        Asaltante asaltante = crearAsaltante();

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asaltante));

        when(asaltanteRepository.save(any(Asaltante.class)))
                .thenReturn(asaltante);

        AsaltanteDTO dto = new AsaltanteDTO();
        dto.setQuitarDeBanda(true);

        AsaltanteDTO resultado
                = asaltanteService.actualizar("ASS00001", dto);

        assertNull(asaltante.getBanda());
        assertNull(resultado.getBandaCodigo());

        verifyNoInteractions(bandaRepository);
    }

    @Test
    void actualizarRechazaAsaltanteInactivo() {
        Asaltante asaltante = crearAsaltante();
        asaltante.setActivo(false);

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asaltante));

        try {
            asaltanteService.actualizar(
                    "ASS00001", new AsaltanteDTO()
            );
            fail("Debía lanzar PersonaInactivaException");
        } catch (PersonaInactivaException e) {
            assertNotNull(e);
        }

        verify(asaltanteRepository, never())
                .save(any(Asaltante.class));
    }

    @Test
    void actualizarRechazaBandaInexistente() {
        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(crearAsaltante()));

        when(bandaRepository.findByCodigo("BAN00002"))
                .thenReturn(Optional.empty());

        AsaltanteDTO dto = new AsaltanteDTO();
        dto.setBandaCodigo("BAN00002");

        try {
            asaltanteService.actualizar("ASS00001", dto);
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }

        verify(asaltanteRepository, never())
                .save(any(Asaltante.class));
    }

    @Test
    void actualizarRechazaBandaInactiva() {
        Asaltante asaltante = crearAsaltante();
        Banda banda = crearBanda();
        banda.setActivo(false);

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asaltante));

        when(bandaRepository.findByCodigo("BAN00002"))
                .thenReturn(Optional.of(banda));

        AsaltanteDTO dto = new AsaltanteDTO();
        dto.setBandaCodigo("BAN00002");

        try {
            asaltanteService.actualizar("ASS00001", dto);
            fail("Debía lanzar BandaInactivaException");
        } catch (BandaInactivaException e) {
            assertNotNull(e);
        }

        verify(asaltanteRepository, never())
                .save(any(Asaltante.class));
    }

    // ELIMINAR
    @Test
    void eliminarAsaltanteCorrectamente() {
        Asaltante asaltante = crearAsaltante();

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asaltante));

        AsaltanteDTO dto = new AsaltanteDTO();
        dto.setMotivoBaja(MotivoBajaPersona.FALLECIMIENTO);

        asaltanteService.eliminar("ASS00001", dto);

        assertFalse(asaltante.isActivo());
        assertEquals(
                MotivoBajaPersona.FALLECIMIENTO,
                asaltante.getMotivoBaja()
        );

        verify(asaltanteRepository).save(asaltante);
    }

    @Test
    void fallecimientoExtingueParticipacionPendiente() {
        Asaltante asaltante = crearAsaltante();
        CasoJudicial caso = mock(CasoJudicial.class);
        when(caso.getId()).thenReturn(10L);
        SentenciaAsaltante participacion = new SentenciaAsaltante(caso, asaltante);
        List<SentenciaAsaltante> participaciones = new ArrayList<>();
        participaciones.add(participacion);

        when(asaltanteRepository.findByCodigo("ASS00001")).thenReturn(Optional.of(asaltante));
        when(sentenciaAsaltanteRepository.findByAsaltante_Id(1L)).thenReturn(participaciones);

        AsaltanteDTO dto = new AsaltanteDTO();
        dto.setMotivoBaja(MotivoBajaPersona.FALLECIMIENTO);
        asaltanteService.eliminar("ASS00001", dto);

        assertEquals(EstadoParticipacionJudicial.EXTINGUIDO_POR_FALLECIMIENTO, participacion.getEstado());
        assertFalse(participacion.isSentenciado());
        assertFalse(asaltante.isActivo());
        verify(sentenciaAsaltanteRepository).saveAll(participaciones);
        verify(casoJudicialService).actualizarEstadoCaso(10L);
    }

    @Test
    void fallecimientoConservaSentenciaRegistrada() {
        Asaltante asaltante = crearAsaltante();
        CasoJudicial caso = mock(CasoJudicial.class);
        when(caso.getId()).thenReturn(10L);
        SentenciaAsaltante participacion = new SentenciaAsaltante(caso, asaltante);
        participacion.registrarSentencia(true, 5);
        List<SentenciaAsaltante> participaciones = new ArrayList<>();
        participaciones.add(participacion);

        when(asaltanteRepository.findByCodigo("ASS00001")).thenReturn(Optional.of(asaltante));
        when(sentenciaAsaltanteRepository.findByAsaltante_Id(1L)).thenReturn(participaciones);

        AsaltanteDTO dto = new AsaltanteDTO();
        dto.setMotivoBaja(MotivoBajaPersona.FALLECIMIENTO);
        asaltanteService.eliminar("ASS00001", dto);

        assertEquals(EstadoParticipacionJudicial.SENTENCIADO, participacion.getEstado());
        assertTrue(participacion.isSentenciado());
        assertTrue(participacion.isCondenado());
        assertEquals(5, participacion.getTiempoCarcel());
        verify(casoJudicialService).actualizarEstadoCaso(10L);
    }

    @Test
    void eliminarExigeMotivoBaja() {
        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(crearAsaltante()));

        try {
            asaltanteService.eliminar("ASS00001", new AsaltanteDTO());
            fail("Debía lanzar MotivoBajaObligatorioException");
        } catch (MotivoBajaObligatorioException e) {
            assertNotNull(e);
        }

        verify(asaltanteRepository, never())
                .save(any(Asaltante.class));
    }

    @Test
    void eliminarRechazaMotivoDiferenteAFallecimiento() {
        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(crearAsaltante()));

        AsaltanteDTO dto = new AsaltanteDTO();
        dto.setMotivoBaja(MotivoBajaPersona.RENUNCIA);

        try {
            asaltanteService.eliminar("ASS00001", dto);
            fail("Debía lanzar MotivoBajaAsaltanteInvalidoException");
        } catch (MotivoBajaAsaltanteInvalidoException e) {
            assertNotNull(e);
        }

        verify(asaltanteRepository, never())
                .save(any(Asaltante.class));
    }

    @Test
    void eliminarAsaltanteYaInactivo() {
        Asaltante asaltante = crearAsaltante();
        asaltante.setActivo(false);

        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.of(asaltante));

        AsaltanteDTO dto = new AsaltanteDTO();
        dto.setMotivoBaja(MotivoBajaPersona.FALLECIMIENTO);

        try {
            asaltanteService.eliminar("ASS00001", dto);
            fail("Debía lanzar PersonaYaInactivaException");
        } catch (PersonaYaInactivaException e) {
            assertNotNull(e);
        }

        verify(asaltanteRepository, never())
                .save(any(Asaltante.class));
    }

    @Test
    void eliminarAsaltanteInexistente() {
        when(asaltanteRepository.findByCodigo("ASS00001"))
                .thenReturn(Optional.empty());

        AsaltanteDTO dto = new AsaltanteDTO();
        dto.setMotivoBaja(MotivoBajaPersona.FALLECIMIENTO);

        try {
            asaltanteService.eliminar("ASS00001", dto);
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }

        verify(asaltanteRepository, never())
                .save(any(Asaltante.class));
    }
}
