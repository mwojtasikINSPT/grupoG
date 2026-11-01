package prog2.policia_backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import prog2.policia_backend.DTOs.AsaltanteDTO;
import prog2.policia_backend.DTOs.BandaDTO;
import prog2.policia_backend.exceptions.BandaConMiembrosException;
import prog2.policia_backend.exceptions.BandaYaActivaException;
import prog2.policia_backend.exceptions.BandaYaInactivaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.models.Banda;
import prog2.policia_backend.repositories.BandaRepository;
import prog2.policia_backend.repositories.AsaltanteRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BandaServiceTest {

    private BandaRepository bandaRepository;
    private AsaltanteRepository asaltanteRepository;
    private BandaService bandaService;

    @BeforeEach
    void setUp() {
        bandaRepository = mock(BandaRepository.class);
        asaltanteRepository = mock(AsaltanteRepository.class);

        bandaService = new BandaService(
                bandaRepository,
                asaltanteRepository
        );
    }

    private Banda crearBanda() {
        Banda banda = new Banda();

        banda.setId(1L);
        banda.setCodigo("BAN00001");
        banda.setActivo(true);

        return banda;
    }

    private void configurarGuardado() {
        when(bandaRepository.save(any(Banda.class)))
                .thenAnswer(new org.mockito.stubbing.Answer<Banda>() {
                    @Override
                    public Banda answer(
                            org.mockito.invocation.InvocationOnMock invocation) {

                        Banda banda = invocation.getArgument(0);

                        if (banda.getId() == null) {
                            banda.setId(1L);
                        }

                        return banda;
                    }
                });
    }

    // LISTAR
    @Test
    void listarTodasLasBandas() {
        List<Banda> bandas = new ArrayList<>();
        bandas.add(crearBanda());

        when(bandaRepository.findAll()).thenReturn(bandas);

        List<BandaDTO> resultado = bandaService.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("BAN00001", resultado.get(0).getCodigo());
        assertTrue(resultado.get(0).getActivo());

        verify(bandaRepository).findAll();
    }

    @Test
    void listarBandasActivas() {
        List<Banda> bandas = new ArrayList<>();
        bandas.add(crearBanda());

        when(bandaRepository.findByActivo(true)).thenReturn(bandas);

        List<BandaDTO> resultado = bandaService.listar(true);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());

        verify(bandaRepository).findByActivo(true);
    }

    @Test
    void listarBandasInactivas() {
        when(bandaRepository.findByActivo(false))
                .thenReturn(new ArrayList<Banda>());

        List<BandaDTO> resultado = bandaService.listar(false);

        assertTrue(resultado.isEmpty());

        verify(bandaRepository).findByActivo(false);
    }

    // BUSCAR POR CODIGO
    @Test
    void buscarPorCodigoCorrectamente() {
        when(bandaRepository.findByCodigo("BAN00001"))
                .thenReturn(Optional.of(crearBanda()));

        BandaDTO resultado = bandaService.buscarPorCodigo("BAN00001");

        assertEquals("BAN00001", resultado.getCodigo());
        assertTrue(resultado.getActivo());
    }

    @Test
    void buscarPorCodigoInexistente() {
        when(bandaRepository.findByCodigo("BAN00001"))
                .thenReturn(Optional.empty());

        try {
            bandaService.buscarPorCodigo("BAN00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // GUARDAR
    @Test
    void guardarBandaCorrectamente() {
        configurarGuardado();

        BandaDTO resultado = bandaService.guardar();

        assertNotNull(resultado);
        assertNotNull(resultado.getCodigo());
        assertTrue(resultado.getCodigo().startsWith("BAN"));
        assertTrue(resultado.getActivo());

        verify(bandaRepository, times(2))
                .save(any(Banda.class));
    }

    // ELIMINAR
    @Test
    void eliminarBandaCorrectamente() {
        Banda banda = crearBanda();

        when(bandaRepository.findByCodigo("BAN00001"))
                .thenReturn(Optional.of(banda));

        when(asaltanteRepository.existsByBanda_IdAndActivoTrue(1L))
                .thenReturn(false);

        bandaService.eliminar("BAN00001");

        assertFalse(banda.isActivo());

        verify(asaltanteRepository)
                .existsByBanda_IdAndActivoTrue(1L);

        verify(bandaRepository).save(banda);
    }

    @Test
    void eliminarBandaConMiembros() {
        Banda banda = crearBanda();

        when(bandaRepository.findByCodigo("BAN00001"))
                .thenReturn(Optional.of(banda));

        when(asaltanteRepository.existsByBanda_IdAndActivoTrue(1L))
                .thenReturn(true);

        try {
            bandaService.eliminar("BAN00001");
            fail("Debía lanzar BandaConMiembrosException");
        } catch (BandaConMiembrosException e) {
            assertNotNull(e);
        }

        assertTrue(banda.isActivo());

        verify(bandaRepository, never())
                .save(any(Banda.class));
    }

    @Test
    void eliminarBandaYaInactiva() {
        Banda banda = crearBanda();
        banda.setActivo(false);

        when(bandaRepository.findByCodigo("BAN00001"))
                .thenReturn(Optional.of(banda));

        try {
            bandaService.eliminar("BAN00001");
            fail("Debía lanzar BandaYaInactivaException");
        } catch (BandaYaInactivaException e) {
            assertNotNull(e);
        }

        verifyNoInteractions(asaltanteRepository);

        verify(bandaRepository, never())
                .save(any(Banda.class));
    }

    @Test
    void eliminarBandaInexistente() {
        when(bandaRepository.findByCodigo("BAN00001"))
                .thenReturn(Optional.empty());

        try {
            bandaService.eliminar("BAN00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }

        verifyNoInteractions(asaltanteRepository);

        verify(bandaRepository, never())
                .save(any(Banda.class));
    }

    // REACTIVAR
    @Test
    void reactivarBandaCorrectamente() {
        Banda banda = crearBanda();
        banda.setActivo(false);

        when(bandaRepository.findByCodigo("BAN00001"))
                .thenReturn(Optional.of(banda));

        when(bandaRepository.save(any(Banda.class)))
                .thenReturn(banda);

        BandaDTO resultado = bandaService.reactivar("BAN00001");

        assertTrue(banda.isActivo());
        assertTrue(resultado.getActivo());
        assertEquals("BAN00001", resultado.getCodigo());

        verify(bandaRepository).save(banda);
    }

    @Test
    void reactivarBandaYaActiva() {
        Banda banda = crearBanda();

        when(bandaRepository.findByCodigo("BAN00001"))
                .thenReturn(Optional.of(banda));

        try {
            bandaService.reactivar("BAN00001");
            fail("Debía lanzar BandaYaActivaException");
        } catch (BandaYaActivaException e) {
            assertNotNull(e);
        }

        verify(bandaRepository, never())
                .save(any(Banda.class));
    }

    @Test
    void reactivarBandaInexistente() {
        when(bandaRepository.findByCodigo("BAN00001"))
                .thenReturn(Optional.empty());

        try {
            bandaService.reactivar("BAN00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }

        verify(bandaRepository, never())
                .save(any(Banda.class));
    }

    @Test
    void listarIntegrantesCorrectamente() {
        Banda banda = crearBanda();
        Asaltante asaltante = new Asaltante();
        asaltante.setId(1L);
        asaltante.setNombre("Juan Perez");
        asaltante.setCodigo("ASA00001");
        asaltante.setActivo(true);

        List<Asaltante> integrantes = new ArrayList<>();
        integrantes.add(asaltante);

        when(bandaRepository.findByCodigo("BAN00001")).thenReturn(Optional.of(banda));
        when(asaltanteRepository.findByBanda_IdAndActivoTrue(1L)).thenReturn(integrantes);

        List<AsaltanteDTO> resultado = bandaService.listarIntegrantes("BAN00001");

        assertEquals(1, resultado.size());
        assertEquals("Juan Perez", resultado.get(0).getNombre());
        assertEquals("ASA00001", resultado.get(0).getCodigo());
        assertEquals("BAN00001", resultado.get(0).getBandaCodigo());
    }

    @Test
    void listarIntegrantesBandaSinMiembros() {
        when(bandaRepository.findByCodigo("BAN00001")).thenReturn(Optional.of(crearBanda()));
        when(asaltanteRepository.findByBanda_IdAndActivoTrue(1L)).thenReturn(new ArrayList<>());

        List<AsaltanteDTO> resultado = bandaService.listarIntegrantes("BAN00001");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void listarIntegrantesBandaInexistente() {
        when(bandaRepository.findByCodigo("BAN99999")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> bandaService.listarIntegrantes("BAN99999"));

        verifyNoInteractions(asaltanteRepository);
    }
}
