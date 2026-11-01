package prog2.policia_backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import prog2.policia_backend.DTOs.EntidadBancariaDTO;
import prog2.policia_backend.exceptions.*;
import prog2.policia_backend.models.*;
import prog2.policia_backend.repositories.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EntidadBancariaServiceTest {

    private EntidadBancariaRepository entidadRepository;
    private SucursalRepository sucursalRepository;
    private EntidadBancariaService entidadService;

    @BeforeEach
    void setUp() {
        entidadRepository = mock(EntidadBancariaRepository.class);
        sucursalRepository = mock(SucursalRepository.class);

        entidadService = new EntidadBancariaService(
                entidadRepository,
                sucursalRepository
        );
    }

    private EntidadBancaria crearEntidad() {
        EntidadBancaria entidad = new EntidadBancaria();

        entidad.setId(1L);
        entidad.setCodigo("EBA00001");
        entidad.setNombre("Banco Nacional");
        entidad.setDomicilioCentral("Avenida Corrientes 123");
        entidad.setActivo(true);

        return entidad;
    }

    private EntidadBancariaDTO crearDTO() {
        EntidadBancariaDTO dto = new EntidadBancariaDTO();

        dto.setNombre("Banco Nacional");
        dto.setDomicilioCentral("Avenida Corrientes 123");

        return dto;
    }

    // LISTAR
    @Test
    void listarTodasLasEntidades() {
        List<EntidadBancaria> entidades = new ArrayList<>();
        entidades.add(crearEntidad());

        when(entidadRepository.findAll()).thenReturn(entidades);

        List<EntidadBancariaDTO> resultado
                = entidadService.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("EBA00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarEntidadesActivas() {
        List<EntidadBancaria> entidades = new ArrayList<>();
        entidades.add(crearEntidad());

        when(entidadRepository.findByActivo(true))
                .thenReturn(entidades);

        List<EntidadBancariaDTO> resultado
                = entidadService.listar(true);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());

        verify(entidadRepository).findByActivo(true);
    }

    @Test
    void listarEntidadesInactivas() {
        when(entidadRepository.findByActivo(false))
                .thenReturn(new ArrayList<EntidadBancaria>());

        List<EntidadBancariaDTO> resultado
                = entidadService.listar(false);

        assertTrue(resultado.isEmpty());
    }

    // BUSCAR POR CODIGO
    @Test
    void buscarPorCodigoCorrectamente() {
        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.of(crearEntidad()));

        EntidadBancariaDTO resultado
                = entidadService.buscarPorCodigo("EBA00001");

        assertEquals("EBA00001", resultado.getCodigo());
        assertEquals("Banco Nacional", resultado.getNombre());
        assertEquals("Avenida Corrientes 123",
                resultado.getDomicilioCentral());
    }

    @Test
    void buscarPorCodigoInexistente() {
        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.empty());

        try {
            entidadService.buscarPorCodigo("EBA00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // BUSCAR POR DOMICILIO
    @Test
    void buscarPorDomicilioEncuentraCoincidencias() {
        List<EntidadBancaria> entidades = new ArrayList<>();
        entidades.add(crearEntidad());

        when(entidadRepository.buscarConFiltros(isNull(), anyString(), isNull())).thenReturn(entidades);

        List<EntidadBancariaDTO> resultado
                = entidadService.buscarPorDomicilio("Corrientes", null);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorDomicilioRespetaEstado() {
        List<EntidadBancaria> entidades = new ArrayList<>();
        entidades.add(crearEntidad());

        when(entidadRepository.buscarConFiltros(isNull(), anyString(), eq(false))).thenReturn(new ArrayList<>());

        List<EntidadBancariaDTO> resultado
                = entidadService.buscarPorDomicilio("Corrientes", false);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorDomicilioSinCoincidencias() {
        when(entidadRepository.buscarConFiltros(isNull(), anyString(), isNull())).thenReturn(new ArrayList<>());

        List<EntidadBancariaDTO> resultado
                = entidadService.buscarPorDomicilio("Rosario", null);

        assertTrue(resultado.isEmpty());
    }

    // BUSCAR POR NOMBRE
    @Test
    void buscarPorNombreEncuentraCoincidencias() {
        List<EntidadBancaria> entidades = new ArrayList<>();
        entidades.add(crearEntidad());

        when(entidadRepository.buscarConFiltros(anyString(), isNull(), isNull())).thenReturn(entidades);

        List<EntidadBancariaDTO> resultado
                = entidadService.buscarPorNombre("nacional", null);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorNombreRespetaEstado() {
        List<EntidadBancaria> entidades = new ArrayList<>();
        entidades.add(crearEntidad());

        when(entidadRepository.buscarConFiltros(anyString(), isNull(), eq(false))).thenReturn(new ArrayList<>());

        List<EntidadBancariaDTO> resultado
                = entidadService.buscarPorNombre("nacional", false);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorNombreSinCoincidencias() {
        when(entidadRepository.buscarConFiltros(anyString(), isNull(), isNull())).thenReturn(new ArrayList<>());

        List<EntidadBancariaDTO> resultado
                = entidadService.buscarPorNombre("Provincial", null);

        assertTrue(resultado.isEmpty());
    }

    // GUARDAR
    @Test
    void guardarEntidadCorrectamente() {
        when(entidadRepository.save(any(EntidadBancaria.class)))
                .thenAnswer(new org.mockito.stubbing.Answer<EntidadBancaria>() {
                    @Override
                    public EntidadBancaria answer(
                            org.mockito.invocation.InvocationOnMock invocation) {

                        EntidadBancaria entidad = invocation.getArgument(0);

                        if (entidad.getId() == null) {
                            entidad.setId(1L);
                        }

                        return entidad;
                    }
                });

        EntidadBancariaDTO resultado
                = entidadService.guardar(crearDTO());

        assertNotNull(resultado);
        assertEquals("EBA00001", resultado.getCodigo());
        assertTrue(resultado.getActivo());
        assertEquals("Banco Nacional", resultado.getNombre());

        verify(entidadRepository, times(2))
                .save(any(EntidadBancaria.class));
    }

    // ACTUALIZAR
    @Test
    void actualizarNombreCorrectamente() {
        EntidadBancaria entidad = crearEntidad();

        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.of(entidad));

        when(entidadRepository.save(any(EntidadBancaria.class)))
                .thenReturn(entidad);

        EntidadBancariaDTO dto = new EntidadBancariaDTO();
        dto.setNombre("Banco Provincial");

        EntidadBancariaDTO resultado
                = entidadService.actualizar("EBA00001", dto);

        assertEquals("Banco Provincial", resultado.getNombre());
        verify(entidadRepository).save(entidad);
    }

    @Test
    void actualizarDomicilioCorrectamente() {
        EntidadBancaria entidad = crearEntidad();

        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.of(entidad));

        when(entidadRepository.save(any(EntidadBancaria.class)))
                .thenReturn(entidad);

        EntidadBancariaDTO dto = new EntidadBancariaDTO();
        dto.setDomicilioCentral("Avenida Santa Fe 456");

        EntidadBancariaDTO resultado
                = entidadService.actualizar("EBA00001", dto);

        assertEquals("Avenida Santa Fe 456",
                resultado.getDomicilioCentral());
    }

    @Test
    void actualizarEntidadInactiva() {
        EntidadBancaria entidad = crearEntidad();
        entidad.setActivo(false);

        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.of(entidad));

        try {
            entidadService.actualizar(
                    "EBA00001", new EntidadBancariaDTO()
            );
            fail("Debía lanzar EntidadInactivaException");
        } catch (EntidadInactivaException e) {
            assertNotNull(e);
        }

        verify(entidadRepository, never())
                .save(any(EntidadBancaria.class));
    }

    @Test
    void actualizarEntidadInexistente() {
        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.empty());

        try {
            entidadService.actualizar(
                    "EBA00001", new EntidadBancariaDTO()
            );
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    @Test
    void guardarRechazaNombreDuplicado() {
        when(entidadRepository.existsByNombreIgnoreCase("Banco Nacional"))
                .thenReturn(true);

        assertThrows(OperacionNoPermitidaException.class,
                () -> entidadService.guardar(crearDTO()));

        verify(entidadRepository, never()).save(any(EntidadBancaria.class));
    }

    @Test
    void actualizarRechazaNombreDeOtraEntidad() {
        EntidadBancaria entidad = crearEntidad();

        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.of(entidad));

        when(entidadRepository.existsByNombreIgnoreCaseAndIdNot("Banco Provincial", 1L))
                .thenReturn(true);

        EntidadBancariaDTO dto = new EntidadBancariaDTO();
        dto.setNombre("Banco Provincial");

        assertThrows(OperacionNoPermitidaException.class,
                () -> entidadService.actualizar("EBA00001", dto));

        assertEquals("Banco Nacional", entidad.getNombre());
        verify(entidadRepository, never()).save(any(EntidadBancaria.class));
    }

    @Test
    void actualizarPermiteConservarNombrePropio() {
        EntidadBancaria entidad = crearEntidad();

        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.of(entidad));

        when(entidadRepository.save(entidad))
                .thenReturn(entidad);

        EntidadBancariaDTO dto = new EntidadBancariaDTO();
        dto.setNombre("Banco Nacional");

        EntidadBancariaDTO resultado = entidadService.actualizar("EBA00001", dto);

        assertEquals("Banco Nacional", resultado.getNombre());
        verify(entidadRepository).save(entidad);
    }

    // ELIMINAR
    @Test
    void eliminarEntidadCorrectamente() {
        EntidadBancaria entidad = crearEntidad();

        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.of(entidad));

        when(sucursalRepository
                .existsByEntidadBancaria_IdAndActivoTrue(1L))
                .thenReturn(false);

        EntidadBancariaDTO dto = new EntidadBancariaDTO();
        dto.setMotivoBaja(MotivoBajaEntidadBancaria.CIERRE_DEFINITIVO);

        entidadService.eliminar("EBA00001", dto);

        assertFalse(entidad.isActivo());
        assertEquals(
                MotivoBajaEntidadBancaria.CIERRE_DEFINITIVO,
                entidad.getMotivoBaja()
        );

        verify(entidadRepository).save(entidad);
    }

    @Test
    void eliminarEntidadYaInactiva() {
        EntidadBancaria entidad = crearEntidad();
        entidad.setActivo(false);

        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.of(entidad));

        EntidadBancariaDTO dto = new EntidadBancariaDTO();
        dto.setMotivoBaja(MotivoBajaEntidadBancaria.QUIEBRA);

        try {
            entidadService.eliminar("EBA00001", dto);
            fail("Debía lanzar EntidadYaInactivaException");
        } catch (EntidadYaInactivaException e) {
            assertNotNull(e);
        }
    }

    @Test
    void eliminarExigeMotivoBaja() {
        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.of(crearEntidad()));

        try {
            entidadService.eliminar(
                    "EBA00001", new EntidadBancariaDTO()
            );
            fail("Debía lanzar MotivoBajaObligatorioException");
        } catch (MotivoBajaObligatorioException e) {
            assertNotNull(e);
        }

        verifyNoInteractions(sucursalRepository);
        verify(entidadRepository, never())
                .save(any(EntidadBancaria.class));
    }

    @Test
    void eliminarEntidadConSucursalesActivas() {
        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.of(crearEntidad()));

        when(sucursalRepository
                .existsByEntidadBancaria_IdAndActivoTrue(1L))
                .thenReturn(true);

        EntidadBancariaDTO dto = new EntidadBancariaDTO();
        dto.setMotivoBaja(MotivoBajaEntidadBancaria.CIERRE_DEFINITIVO);

        try {
            entidadService.eliminar("EBA00001", dto);
            fail("Debía lanzar EntidadBancariaConSucursalesException");
        } catch (EntidadBancariaConSucursalesException e) {
            assertNotNull(e);
        }

        verify(entidadRepository, never())
                .save(any(EntidadBancaria.class));
    }

    @Test
    void eliminarEntidadInexistente() {
        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.empty());

        EntidadBancariaDTO dto = new EntidadBancariaDTO();
        dto.setMotivoBaja(MotivoBajaEntidadBancaria.QUIEBRA);

        try {
            entidadService.eliminar("EBA00001", dto);
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // REACTIVAR
    @Test
    void reactivarEntidadCorrectamente() {
        EntidadBancaria entidad = crearEntidad();
        entidad.setActivo(false);
        entidad.setMotivoBaja(MotivoBajaEntidadBancaria.QUIEBRA);

        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.of(entidad));

        when(entidadRepository.save(any(EntidadBancaria.class)))
                .thenReturn(entidad);

        EntidadBancariaDTO resultado
                = entidadService.reactivar("EBA00001");

        assertTrue(entidad.isActivo());
        assertNull(entidad.getMotivoBaja());
        assertTrue(resultado.getActivo());
    }

    @Test
    void reactivarEntidadYaActiva() {
        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.of(crearEntidad()));

        try {
            entidadService.reactivar("EBA00001");
            fail("Debía lanzar EntidadYaActivaException");
        } catch (EntidadYaActivaException e) {
            assertNotNull(e);
        }

        verify(entidadRepository, never())
                .save(any(EntidadBancaria.class));
    }

    @Test
    void reactivarEntidadInexistente() {
        when(entidadRepository.findByCodigo("EBA00001"))
                .thenReturn(Optional.empty());

        try {
            entidadService.reactivar("EBA00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // BUSCAR CON FILTROS
    @Test
    void buscarConFiltrosPorNombre() {
        List<EntidadBancaria> entidades = new ArrayList<>();
        entidades.add(crearEntidad());

        when(entidadRepository.buscarConFiltros(anyString(), isNull(), isNull())).thenReturn(entidades);

        List<EntidadBancariaDTO> resultado
                = entidadService.buscarConFiltros("Nacional", null, null);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarConFiltrosPorDomicilio() {
        List<EntidadBancaria> entidades = new ArrayList<>();
        entidades.add(crearEntidad());

        when(entidadRepository.buscarConFiltros(isNull(), anyString(), isNull())).thenReturn(entidades);

        List<EntidadBancariaDTO> resultado
                = entidadService.buscarConFiltros(null, "Corrientes", null);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarConFiltrosPorEstado() {
        List<EntidadBancaria> entidades = new ArrayList<>();
        entidades.add(crearEntidad());

        when(entidadRepository.buscarConFiltros(isNull(), isNull(), eq(true))).thenReturn(entidades);

        List<EntidadBancariaDTO> resultado
                = entidadService.buscarConFiltros(null, null, true);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarConFiltrosCombinados() {
        List<EntidadBancaria> entidades = new ArrayList<>();
        entidades.add(crearEntidad());

        when(entidadRepository.buscarConFiltros(anyString(), anyString(), eq(true))).thenReturn(entidades);

        List<EntidadBancariaDTO> resultado
                = entidadService.buscarConFiltros("Nacional", "Corrientes", true);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarConFiltrosSinCoincidencias() {
        List<EntidadBancaria> entidades = new ArrayList<>();
        entidades.add(crearEntidad());

        when(entidadRepository.buscarConFiltros(anyString(), isNull(), eq(true))).thenReturn(new ArrayList<>());

        List<EntidadBancariaDTO> resultado
                = entidadService.buscarConFiltros("Provincial", null, true);

        assertTrue(resultado.isEmpty());
    }
}
