package prog2.policia_backend.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import prog2.policia_backend.DTOs.ContratoVigilanciaDTO;
import prog2.policia_backend.exceptions.*;
import prog2.policia_backend.models.*;
import prog2.policia_backend.repositories.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContratoVigilanciaServiceTest {

    private ContratoVigilanciaRepository contratoRepository;
    private VigilanteRepository vigilanteRepository;
    private SucursalRepository sucursalRepository;
    private ContratoVigilanciaService contratoService;

    @BeforeEach
    void setUp() {
        contratoRepository = mock(ContratoVigilanciaRepository.class);
        vigilanteRepository = mock(VigilanteRepository.class);
        sucursalRepository = mock(SucursalRepository.class);

        contratoService = new ContratoVigilanciaService(
                contratoRepository,
                vigilanteRepository,
                sucursalRepository
        );
    }

    private Vigilante crearVigilante() {
        Vigilante vigilante = new Vigilante();
        vigilante.setId(1L);
        vigilante.setCodigo("VIG00001");
        vigilante.setNombre("Juan Perez");
        vigilante.setEdad(30);
        vigilante.setActivo(true);

        Rol rol = new Rol();
        rol.setNombre("VIGILANTE");
        rol.setPrefijo("VIG");
        vigilante.setRol(rol);

        return vigilante;
    }

    private Sucursal crearSucursal() {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setCodigo("SUC00001");
        sucursal.setDomicilio("Av. Siempre Viva 123");
        sucursal.setActivo(true);

        return sucursal;
    }

    private ContratoVigilancia crearContrato(
            String codigo,
            LocalDate fecha,
            boolean activo) {

        ContratoVigilancia contrato = new ContratoVigilancia();

        contrato.setId(1L);
        contrato.setCodigo(codigo);
        contrato.setFecha(fecha);
        contrato.setConArma(true);
        contrato.setVigilante(crearVigilante());
        contrato.setSucursal(crearSucursal());
        contrato.setActivo(activo);

        return contrato;
    }

    private ContratoVigilanciaDTO crearDTO(LocalDate fecha) {
        ContratoVigilanciaDTO dto = new ContratoVigilanciaDTO();

        dto.setFecha(fecha);
        dto.setConArma(true);
        dto.setVigilanteCodigo("VIG00001");
        dto.setSucursalCodigo("SUC00001");

        return dto;
    }

    private void configurarGuardado() {
        when(contratoRepository.save(any(ContratoVigilancia.class)))
                .thenAnswer(new org.mockito.stubbing.Answer<ContratoVigilancia>() {
                    @Override
                    public ContratoVigilancia answer(
                            org.mockito.invocation.InvocationOnMock invocation) {

                        ContratoVigilancia contrato
                                = invocation.getArgument(0);

                        if (contrato.getId() == null) {
                            contrato.setId(1L);
                        }

                        return contrato;
                    }
                });
    }

    // LISTAR
    @Test
    void listarTodosLosContratos() {
        List<ContratoVigilancia> contratos = new ArrayList<>();
        contratos.add(crearContrato(
                "CDV00001", LocalDate.now().plusDays(5), true
        ));

        when(contratoRepository.findAll())
                .thenReturn(contratos);

        List<ContratoVigilanciaDTO> resultado
                = contratoService.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("CDV00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarContratosActivos() {
        List<ContratoVigilancia> contratos = new ArrayList<>();
        contratos.add(crearContrato(
                "CDV00001", LocalDate.now().plusDays(5), true
        ));

        when(contratoRepository.findByActivo(true))
                .thenReturn(contratos);

        List<ContratoVigilanciaDTO> resultado
                = contratoService.listar(true);

        assertEquals(1, resultado.size());
        verify(contratoRepository).findByActivo(true);
    }

    @Test
    void listarContratosInactivos() {
        when(contratoRepository.findByActivo(false))
                .thenReturn(new ArrayList<ContratoVigilancia>());

        List<ContratoVigilanciaDTO> resultado
                = contratoService.listar(false);

        assertTrue(resultado.isEmpty());
    }

    // BUSCAR
    @Test
    void buscarPorCodigoEncuentraContrato() {
        ContratoVigilancia contrato = crearContrato(
                "CDV00001", LocalDate.now().plusDays(5), true
        );

        when(contratoRepository.findByCodigo("CDV00001"))
                .thenReturn(Optional.of(contrato));

        ContratoVigilanciaDTO resultado
                = contratoService.buscarPorCodigo("CDV00001");

        assertEquals("CDV00001", resultado.getCodigo());
        assertEquals("VIG00001", resultado.getVigilanteCodigo());
        assertEquals("SUC00001", resultado.getSucursalCodigo());
    }

    @Test
    void buscarPorCodigoInexistenteLanzaExcepcion() {
        when(contratoRepository.findByCodigo("CDV00001"))
                .thenReturn(Optional.empty());

        try {
            contratoService.buscarPorCodigo("CDV00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    @Test
    void buscarPorNombreVigilanteEncuentraContratos() {
        List<ContratoVigilancia> contratos = new ArrayList<>();
        contratos.add(crearContrato(
                "CDV00001", LocalDate.now().plusDays(5), true
        ));

        when(contratoRepository.buscarPorNombreVigilante(
                anyString(), nullable(Boolean.class)))
                .thenReturn(contratos);

        List<ContratoVigilanciaDTO> resultado
                = contratoService.buscarPorNombreVigilante("juan", null);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorNombreVigilanteRespetaEstado() {
        when(contratoRepository.buscarPorNombreVigilante("juan", false))
                .thenReturn(new ArrayList<>());

        List<ContratoVigilanciaDTO> resultado
                = contratoService.buscarPorNombreVigilante("Juan", false);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorNombreVigilanteSinCoincidencias() {
        when(contratoRepository.buscarPorNombreVigilante(
                anyString(), nullable(Boolean.class)))
                .thenReturn(new ArrayList<>());

        List<ContratoVigilanciaDTO> resultado
                = contratoService.buscarPorNombreVigilante("Pedro", null);

        assertTrue(resultado.isEmpty());
    }

    // GUARDAR
    @Test
    void guardarContratoCorrectamente() {
        Vigilante vigilante = crearVigilante();
        Sucursal sucursal = crearSucursal();

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(sucursal));

        when(contratoRepository
                .existsByVigilante_IdAndFechaAndActivoTrue(
                        1L, LocalDate.now().plusDays(5)))
                .thenReturn(false);

        configurarGuardado();

        ContratoVigilanciaDTO resultado
                = contratoService.guardar(
                        crearDTO(LocalDate.now().plusDays(5))
                );

        assertNotNull(resultado);
        assertNotNull(resultado.getCodigo());
        assertTrue(resultado.getActivo());
        assertEquals("VIG00001", resultado.getVigilanteCodigo());
        assertEquals("SUC00001", resultado.getSucursalCodigo());

        verify(contratoRepository, times(2))
                .save(any(ContratoVigilancia.class));
    }

    @Test
    void guardarRechazaVigilanteInexistente() {
        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.empty());

        try {
            contratoService.guardar(
                    crearDTO(LocalDate.now().plusDays(5))
            );
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    @Test
    void guardarRechazaVigilanteInactivo() {
        Vigilante vigilante = crearVigilante();
        vigilante.setActivo(false);

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        try {
            contratoService.guardar(
                    crearDTO(LocalDate.now().plusDays(5))
            );
            fail("Debía lanzar PersonaInactivaException");
        } catch (PersonaInactivaException e) {
            assertNotNull(e);
        }
    }

    @Test
    void guardarRechazaSucursalInexistente() {
        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(crearVigilante()));

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.empty());

        try {
            contratoService.guardar(
                    crearDTO(LocalDate.now().plusDays(5))
            );
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    @Test
    void guardarRechazaSucursalCerrada() {
        Sucursal sucursal = crearSucursal();
        sucursal.setActivo(false);

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(crearVigilante()));

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(sucursal));

        try {
            contratoService.guardar(
                    crearDTO(LocalDate.now().plusDays(5))
            );
            fail("Debía lanzar SucursalYaCerradaException");
        } catch (SucursalYaCerradaException e) {
            assertNotNull(e);
        }
    }

    @Test
    void guardarRechazaContratoDuplicado() {
        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(crearVigilante()));

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(crearSucursal()));

        when(contratoRepository
                .existsByVigilante_IdAndFechaAndActivoTrue(
                        1L, LocalDate.now().plusDays(5)))
                .thenReturn(true);

        try {
            contratoService.guardar(
                    crearDTO(LocalDate.now().plusDays(5))
            );
            fail("Debía lanzar ContratoVigilanciaDuplicadoException");
        } catch (ContratoVigilanciaDuplicadoException e) {
            assertNotNull(e);
        }

        verify(contratoRepository, never())
                .save(any(ContratoVigilancia.class));
    }

    // ACTUALIZAR
    @Test
    void actualizarContratoCorrectamente() {
        ContratoVigilancia contrato = crearContrato(
                "CDV00001", LocalDate.now().plusDays(5), true
        );

        when(contratoRepository.findByCodigo("CDV00001"))
                .thenReturn(Optional.of(contrato));

        when(contratoRepository
                .existsByVigilante_IdAndFechaAndActivoTrueAndIdNot(
                        1L, contrato.getFecha(), 1L))
                .thenReturn(false);

        when(contratoRepository.save(any(ContratoVigilancia.class)))
                .thenReturn(contrato);

        ContratoVigilanciaDTO dto = new ContratoVigilanciaDTO();
        dto.setConArma(false);

        ContratoVigilanciaDTO resultado
                = contratoService.actualizar("CDV00001", dto);

        assertFalse(resultado.getConArma());
        verify(contratoRepository).save(contrato);
    }

    @Test
    void actualizarRechazaContratoInactivo() {
        ContratoVigilancia contrato = crearContrato(
                "CDV00001", LocalDate.now().plusDays(5), false
        );

        when(contratoRepository.findByCodigo("CDV00001"))
                .thenReturn(Optional.of(contrato));

        try {
            contratoService.actualizar(
                    "CDV00001", new ContratoVigilanciaDTO()
            );
            fail("Debía lanzar ContratoVigilanciaCumplidoException");
        } catch (ContratoVigilanciaCumplidoException e) {
            assertNotNull(e);
        }
    }

    @Test
    void actualizarRechazaFechaPasada() {
        ContratoVigilancia contrato = crearContrato(
                "CDV00001", LocalDate.now().plusDays(5), true
        );

        when(contratoRepository.findByCodigo("CDV00001"))
                .thenReturn(Optional.of(contrato));

        ContratoVigilanciaDTO dto = new ContratoVigilanciaDTO();
        dto.setFecha(LocalDate.now().minusDays(1));

        try {
            contratoService.actualizar("CDV00001", dto);
            fail("Debía lanzar FechaPasadaException");
        } catch (FechaPasadaException e) {
            assertNotNull(e);
        }
    }

    @Test
    void actualizarRechazaContratoDuplicado() {
        ContratoVigilancia contrato = crearContrato(
                "CDV00001", LocalDate.now().plusDays(5), true
        );

        when(contratoRepository.findByCodigo("CDV00001"))
                .thenReturn(Optional.of(contrato));

        when(contratoRepository
                .existsByVigilante_IdAndFechaAndActivoTrueAndIdNot(
                        1L, contrato.getFecha(), 1L))
                .thenReturn(true);

        try {
            contratoService.actualizar(
                    "CDV00001", new ContratoVigilanciaDTO()
            );
            fail("Debía lanzar ContratoVigilanciaDuplicadoException");
        } catch (ContratoVigilanciaDuplicadoException e) {
            assertNotNull(e);
        }
    }

    @Test
    void actualizarPermiteCambiarVigilante() {
        ContratoVigilancia contrato = crearContrato(
                "CDV00001", LocalDate.now().plusDays(5), true
        );

        Vigilante nuevoVigilante = crearVigilante();
        nuevoVigilante.setId(2L);
        nuevoVigilante.setCodigo("VIG00002");

        when(contratoRepository.findByCodigo("CDV00001"))
                .thenReturn(Optional.of(contrato));

        when(vigilanteRepository.findByCodigo("VIG00002"))
                .thenReturn(Optional.of(nuevoVigilante));

        when(contratoRepository
                .existsByVigilante_IdAndFechaAndActivoTrueAndIdNot(
                        2L, contrato.getFecha(), 1L))
                .thenReturn(false);

        when(contratoRepository.save(any(ContratoVigilancia.class)))
                .thenReturn(contrato);

        ContratoVigilanciaDTO dto = new ContratoVigilanciaDTO();
        dto.setVigilanteCodigo("VIG00002");

        contratoService.actualizar("CDV00001", dto);

        assertEquals("VIG00002", contrato.getVigilante().getCodigo());
    }

    @Test
    void actualizarPermiteCambiarSucursal() {
        ContratoVigilancia contrato = crearContrato(
                "CDV00001", LocalDate.now().plusDays(5), true
        );

        Sucursal nuevaSucursal = crearSucursal();
        nuevaSucursal.setId(2L);
        nuevaSucursal.setCodigo("SUC00002");

        when(contratoRepository.findByCodigo("CDV00001"))
                .thenReturn(Optional.of(contrato));

        when(contratoRepository
                .existsByVigilante_IdAndFechaAndActivoTrueAndIdNot(
                        1L, contrato.getFecha(), 1L))
                .thenReturn(false);

        when(sucursalRepository.findByCodigo("SUC00002"))
                .thenReturn(Optional.of(nuevaSucursal));

        when(contratoRepository.save(any(ContratoVigilancia.class)))
                .thenReturn(contrato);

        ContratoVigilanciaDTO dto = new ContratoVigilanciaDTO();
        dto.setSucursalCodigo("SUC00002");

        contratoService.actualizar("CDV00001", dto);

        assertEquals("SUC00002", contrato.getSucursal().getCodigo());
    }

    // ELIMINAR
    @Test
    void eliminarContratoCorrectamente() {
        ContratoVigilancia contrato = crearContrato(
                "CDV00001", LocalDate.now().plusDays(5), true
        );

        when(contratoRepository.findByCodigo("CDV00001"))
                .thenReturn(Optional.of(contrato));

        contratoService.eliminar("CDV00001");

        assertFalse(contrato.getActivo());
        assertEquals(
                MotivoBajaContrato.CANCELACION,
                contrato.getMotivoBaja()
        );

        verify(contratoRepository).save(contrato);
    }

    @Test
    void eliminarRechazaContratoInactivo() {
        ContratoVigilancia contrato = crearContrato(
                "CDV00001", LocalDate.now().plusDays(5), false
        );

        when(contratoRepository.findByCodigo("CDV00001"))
                .thenReturn(Optional.of(contrato));

        try {
            contratoService.eliminar("CDV00001");
            fail("Debía lanzar ContratoVigilanciaCumplidoException");
        } catch (ContratoVigilanciaCumplidoException e) {
            assertNotNull(e);
        }
    }

    @Test
    void eliminarRechazaContratoVencido() {
        ContratoVigilancia contrato = crearContrato(
                "CDV00001", LocalDate.now().minusDays(1), true
        );

        when(contratoRepository.findByCodigo("CDV00001"))
                .thenReturn(Optional.of(contrato));

        try {
            contratoService.eliminar("CDV00001");
            fail("Debía lanzar ContratoVigilanciaCumplidoException");
        } catch (ContratoVigilanciaCumplidoException e) {
            assertNotNull(e);
        }
    }

    // LISTAR POR VIGILANTE
    @Test
    void listarContratosPorVigilante() {
        Vigilante vigilante = crearVigilante();

        List<ContratoVigilancia> contratos = new ArrayList<>();
        contratos.add(crearContrato(
                "CDV00001", LocalDate.now().plusDays(5), true
        ));

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        when(contratoRepository.findByVigilante_Id(1L))
                .thenReturn(contratos);

        List<ContratoVigilanciaDTO> resultado
                = contratoService.listarPorVigilante("VIG00001");

        assertEquals(1, resultado.size());
        assertEquals("CDV00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarContratosRechazaVigilanteInexistente() {
        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.empty());

        try {
            contratoService.listarPorVigilante("VIG00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // LISTAR POR SUCURSAL
    @Test
    void listarContratosPorSucursal() {
        Sucursal sucursal = crearSucursal();

        List<ContratoVigilancia> contratos = new ArrayList<>();
        contratos.add(crearContrato(
                "CDV00001", LocalDate.now().plusDays(5), true
        ));

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(sucursal));

        when(contratoRepository.findBySucursal_Id(1L))
                .thenReturn(contratos);

        List<ContratoVigilanciaDTO> resultado
                = contratoService.listarPorSucursal("SUC00001");

        assertEquals(1, resultado.size());
        assertEquals("CDV00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarContratosRechazaSucursalInexistente() {
        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.empty());

        try {
            contratoService.listarPorSucursal("SUC00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }
}
