package prog2.policia_backend.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import prog2.policia_backend.DTOs.JuezDTO;
import prog2.policia_backend.exceptions.*;
import prog2.policia_backend.models.Juez;
import prog2.policia_backend.models.MotivoBajaPersona;
import prog2.policia_backend.repositories.JuezRepository;
import prog2.policia_backend.repositories.CasoJudicialRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JuezServiceTest {

    private JuezRepository juezRepository;
    private CasoJudicialRepository casoJudicialRepository;
    private JuezService juezService;

    @BeforeEach
    void setUp() {
        juezRepository = mock(JuezRepository.class);
        casoJudicialRepository = mock(CasoJudicialRepository.class);

        juezService = new JuezService(
                juezRepository,
                casoJudicialRepository
        );
    }

    private Juez crearJuez() {
        Juez juez = new Juez();

        juez.setId(1L);
        juez.setCodigo("JUE00001");
        juez.setNombre("Juan Perez");
        juez.setJuezDesde(LocalDate.now().minusYears(10));
        juez.setActivo(true);

        return juez;
    }

    private JuezDTO crearDTO() {
        JuezDTO dto = new JuezDTO();

        dto.setNombre("Juan Perez");
        dto.setJuezDesde(LocalDate.now().minusYears(10));

        return dto;
    }

    // LISTAR
    @Test
    void listarTodosLosJueces() {
        List<Juez> jueces = new ArrayList<>();
        jueces.add(crearJuez());

        when(juezRepository.findAll()).thenReturn(jueces);

        List<JuezDTO> resultado = juezService.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("JUE00001", resultado.get(0).getCodigo());
        assertEquals("Juan Perez", resultado.get(0).getNombre());
    }

    @Test
    void listarJuecesActivos() {
        List<Juez> jueces = new ArrayList<>();
        jueces.add(crearJuez());

        when(juezRepository.findByActivo(true)).thenReturn(jueces);

        List<JuezDTO> resultado = juezService.listar(true);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());

        verify(juezRepository).findByActivo(true);
    }

    @Test
    void listarJuecesInactivos() {
        when(juezRepository.findByActivo(false))
                .thenReturn(new ArrayList<Juez>());

        List<JuezDTO> resultado = juezService.listar(false);

        assertTrue(resultado.isEmpty());
    }

    // BUSCAR
    @Test
    void buscarPorCodigoCorrectamente() {
        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.of(crearJuez()));

        JuezDTO resultado = juezService.buscarPorCodigo("JUE00001");

        assertEquals("JUE00001", resultado.getCodigo());
        assertEquals("Juan Perez", resultado.getNombre());
        assertEquals(10, resultado.getAniosServicio());
    }

    @Test
    void buscarPorCodigoInexistente() {
        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.empty());

        try {
            juezService.buscarPorCodigo("JUE00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // BUSCAR POR NOMBRE
    @Test
    void buscarPorNombreCorrectamente() {
        List<Juez> jueces = new ArrayList<>();
        jueces.add(crearJuez());

        when(juezRepository.findByNombreContainingIgnoreCase(anyString())).thenReturn(jueces);

        List<JuezDTO> resultado = juezService.buscarPorNombre("juan", null);

        assertEquals(1, resultado.size());
        assertEquals("Juan Perez", resultado.get(0).getNombre());

        verify(juezRepository).findByNombreContainingIgnoreCase(anyString());
    }

    @Test
    void buscarPorNombreRespetaEstado() {
        when(juezRepository.findByNombreContainingIgnoreCaseAndActivo(anyString(), eq(false))).thenReturn(new ArrayList<>());

        List<JuezDTO> resultado = juezService.buscarPorNombre("Juan", false);

        assertTrue(resultado.isEmpty());

        verify(juezRepository).findByNombreContainingIgnoreCaseAndActivo(anyString(), eq(false));
    }

    @Test
    void buscarPorNombreSinCoincidencias() {
        when(juezRepository.findByNombreContainingIgnoreCase(anyString())).thenReturn(new ArrayList<>());

        List<JuezDTO> resultado = juezService.buscarPorNombre("Pedro", null);

        assertTrue(resultado.isEmpty());
    }

    // GUARDAR
    @Test
    void guardarJuezCorrectamente() {
        when(juezRepository.save(any(Juez.class)))
                .thenAnswer(new org.mockito.stubbing.Answer<Juez>() {
                    @Override
                    public Juez answer(
                            org.mockito.invocation.InvocationOnMock invocation) {

                        Juez juez = invocation.getArgument(0);

                        if (juez.getId() == null) {
                            juez.setId(1L);
                        }

                        return juez;
                    }
                });

        JuezDTO resultado = juezService.guardar(crearDTO());

        assertNotNull(resultado);
        assertEquals("JUE00001", resultado.getCodigo());
        assertEquals("Juan Perez", resultado.getNombre());
        assertTrue(resultado.getActivo());

        verify(juezRepository, times(2)).save(any(Juez.class));
    }

    @Test
    void guardarCalculaAniosDeServicio() {
        JuezDTO dto = crearDTO();
        dto.setJuezDesde(LocalDate.now().minusYears(15));

        when(juezRepository.save(any(Juez.class)))
                .thenAnswer(new org.mockito.stubbing.Answer<Juez>() {
                    @Override
                    public Juez answer(
                            org.mockito.invocation.InvocationOnMock invocation) {

                        Juez juez = invocation.getArgument(0);

                        if (juez.getId() == null) {
                            juez.setId(2L);
                        }

                        return juez;
                    }
                });

        JuezDTO resultado = juezService.guardar(dto);

        assertEquals(15, resultado.getAniosServicio());
    }

    // ACTUALIZAR
    @Test
    void actualizarNombreCorrectamente() {
        Juez juez = crearJuez();

        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.of(juez));

        when(juezRepository.save(any(Juez.class)))
                .thenReturn(juez);

        JuezDTO dto = new JuezDTO();
        dto.setNombre("Carlos Rodriguez");

        JuezDTO resultado
                = juezService.actualizar("JUE00001", dto);

        assertEquals("Carlos Rodriguez", resultado.getNombre());
        verify(juezRepository).save(juez);
    }

    @Test
    void actualizarFechaCorrectamente() {
        Juez juez = crearJuez();

        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.of(juez));

        when(juezRepository.save(any(Juez.class)))
                .thenReturn(juez);

        LocalDate nuevaFecha = LocalDate.now().minusYears(5);

        JuezDTO dto = new JuezDTO();
        dto.setJuezDesde(nuevaFecha);

        JuezDTO resultado
                = juezService.actualizar("JUE00001", dto);

        assertEquals(nuevaFecha, resultado.getJuezDesde());
        assertEquals(5, resultado.getAniosServicio());
    }

    @Test
    void actualizarRechazaFechaFutura() {
        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.of(crearJuez()));

        JuezDTO dto = new JuezDTO();
        dto.setJuezDesde(LocalDate.now().plusDays(1));

        try {
            juezService.actualizar("JUE00001", dto);
            fail("Debía lanzar FechaFuturaException");
        } catch (FechaFuturaException e) {
            assertNotNull(e);
        }

        verify(juezRepository, never()).save(any(Juez.class));
    }

    @Test
    void actualizarRechazaJuezInactivo() {
        Juez juez = crearJuez();
        juez.setActivo(false);

        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.of(juez));

        try {
            juezService.actualizar("JUE00001", new JuezDTO());
            fail("Debía lanzar PersonaInactivaException");
        } catch (PersonaInactivaException e) {
            assertNotNull(e);
        }

        verify(juezRepository, never()).save(any(Juez.class));
    }

    @Test
    void actualizarJuezInexistente() {
        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.empty());

        try {
            juezService.actualizar("JUE00001", new JuezDTO());
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // ELIMINAR
    @Test
    void eliminarJuezCorrectamente() {
        Juez juez = crearJuez();

        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.of(juez));

        when(casoJudicialRepository.existsByJuez_Id(1L))
                .thenReturn(false);

        JuezDTO dto = new JuezDTO();
        dto.setMotivoBaja(MotivoBajaPersona.JUBILACION);

        juezService.eliminar("JUE00001", dto);

        assertFalse(juez.isActivo());
        assertEquals(
                MotivoBajaPersona.JUBILACION,
                juez.getMotivoBaja()
        );

        verify(juezRepository).save(juez);
    }

    @Test
    void eliminarExigeMotivoBaja() {
        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.of(crearJuez()));

        try {
            juezService.eliminar("JUE00001", new JuezDTO());
            fail("Debía lanzar MotivoBajaObligatorioException");
        } catch (MotivoBajaObligatorioException e) {
            assertNotNull(e);
        }

        verify(juezRepository, never()).save(any(Juez.class));
    }

    @Test
    void eliminarRechazaJuezInactivo() {
        Juez juez = crearJuez();
        juez.setActivo(false);

        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.of(juez));

        JuezDTO dto = new JuezDTO();
        dto.setMotivoBaja(MotivoBajaPersona.JUBILACION);

        try {
            juezService.eliminar("JUE00001", dto);
            fail("Debía lanzar PersonaYaInactivaException");
        } catch (PersonaYaInactivaException e) {
            assertNotNull(e);
        }

        verify(juezRepository, never()).save(any(Juez.class));
    }

    @Test
    void eliminarRechazaJuezConCasosJudiciales() {
        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.of(crearJuez()));

        when(casoJudicialRepository.existsByJuez_Id(1L))
                .thenReturn(true);

        JuezDTO dto = new JuezDTO();
        dto.setMotivoBaja(MotivoBajaPersona.JUBILACION);

        try {
            juezService.eliminar("JUE00001", dto);
            fail("Debía lanzar JuezConCasosJudicialesException");
        } catch (JuezConCasosJudicialesException e) {
            assertNotNull(e);
        }

        verify(juezRepository, never()).save(any(Juez.class));
    }

    @Test
    void eliminarJuezInexistente() {
        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.empty());

        JuezDTO dto = new JuezDTO();
        dto.setMotivoBaja(MotivoBajaPersona.JUBILACION);

        try {
            juezService.eliminar("JUE00001", dto);
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // REACTIVAR
    @Test
    void reactivarJuezCorrectamente() {
        Juez juez = crearJuez();
        juez.setActivo(false);
        juez.setMotivoBaja(MotivoBajaPersona.JUBILACION);

        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.of(juez));

        when(juezRepository.save(any(Juez.class)))
                .thenReturn(juez);

        JuezDTO resultado = juezService.reactivar("JUE00001");

        assertTrue(resultado.getActivo());
        assertNull(resultado.getMotivoBaja());

        verify(juezRepository).save(juez);
    }

    @Test
    void reactivarRechazaJuezFallecido() {
        Juez juez = crearJuez();
        juez.setActivo(false);
        juez.setMotivoBaja(MotivoBajaPersona.FALLECIMIENTO);

        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.of(juez));

        try {
            juezService.reactivar("JUE00001");
            fail("Debía lanzar PersonaNoReactivableException");
        } catch (PersonaNoReactivableException e) {
            assertNotNull(e);
        }

        verify(juezRepository, never()).save(any(Juez.class));
    }

    @Test
    void reactivarJuezYaActivo() {
        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.of(crearJuez()));

        try {
            juezService.reactivar("JUE00001");
            fail("Debía lanzar PersonaYaActivaException");
        } catch (PersonaYaActivaException e) {
            assertNotNull(e);
        }

        verify(juezRepository, never()).save(any(Juez.class));
    }

    @Test
    void reactivarJuezInexistente() {
        when(juezRepository.findByCodigo("JUE00001"))
                .thenReturn(Optional.empty());

        try {
            juezService.reactivar("JUE00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }
}
