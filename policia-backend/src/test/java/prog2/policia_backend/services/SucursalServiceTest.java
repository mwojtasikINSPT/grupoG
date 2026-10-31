package prog2.policia_backend.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import prog2.policia_backend.DTOs.SucursalDTO;
import prog2.policia_backend.exceptions.*;
import prog2.policia_backend.models.*;
import prog2.policia_backend.repositories.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SucursalServiceTest {

    private SucursalRepository sucursalRepository;
    private EntidadBancariaRepository entidadBancariaRepository;
    private ContratoVigilanciaRepository contratoRepository;
    private SucursalService sucursalService;

    @BeforeEach
    void setUp() {
        sucursalRepository = mock(SucursalRepository.class);
        entidadBancariaRepository = mock(EntidadBancariaRepository.class);
        contratoRepository = mock(ContratoVigilanciaRepository.class);

        sucursalService = new SucursalService(
                sucursalRepository,
                entidadBancariaRepository,
                contratoRepository
        );
    }

    private EntidadBancaria crearEntidad() {
        EntidadBancaria entidad = new EntidadBancaria();
        entidad.setId(1L);
        entidad.setCodigo("ENB00001");
        entidad.setNombre("Banco Nacional");
        entidad.setDomicilioCentral("Buenos Aires");
        entidad.setActivo(true);
        return entidad;
    }

    private Sucursal crearSucursal() {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setCodigo("SUC00001");
        sucursal.setDomicilio("Av. Siempre Viva 123");
        sucursal.setCantEmpleados(10);
        sucursal.setEntidadBancaria(crearEntidad());
        sucursal.setActivo(true);
        return sucursal;
    }

    private SucursalDTO crearDTO() {
        SucursalDTO dto = new SucursalDTO();
        dto.setDomicilio("Av. Siempre Viva 123");
        dto.setCantEmpleados(10);
        dto.setEntidadBancariaCodigo("ENB00001");
        return dto;
    }

    private ContratoVigilancia crearContrato(Sucursal sucursal) {
        ContratoVigilancia contrato = new ContratoVigilancia();
        contrato.setId(1L);
        contrato.setCodigo("CDV00001");
        contrato.setFecha(LocalDate.now().plusDays(10));
        contrato.setSucursal(sucursal);
        contrato.setActivo(true);
        return contrato;
    }

    private void configurarEntidadActiva() {
        when(entidadBancariaRepository.findByCodigo("ENB00001"))
                .thenReturn(Optional.of(crearEntidad()));
    }

    // LISTAR
    @Test
    void listarTodasLasSucursales() {
        List<Sucursal> sucursales = new ArrayList<>();
        sucursales.add(crearSucursal());

        when(sucursalRepository.findAll()).thenReturn(sucursales);

        List<SucursalDTO> resultado = sucursalService.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("SUC00001", resultado.get(0).getCodigo());
        assertEquals("ENB00001", resultado.get(0).getEntidadBancariaCodigo());
    }

    @Test
    void listarSucursalesActivas() {
        List<Sucursal> sucursales = new ArrayList<>();
        sucursales.add(crearSucursal());

        when(sucursalRepository.findByActivo(true))
                .thenReturn(sucursales);

        List<SucursalDTO> resultado = sucursalService.listar(true);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());
    }

    @Test
    void listarSucursalesInactivas() {
        when(sucursalRepository.findByActivo(false))
                .thenReturn(new ArrayList<Sucursal>());

        List<SucursalDTO> resultado = sucursalService.listar(false);

        assertTrue(resultado.isEmpty());
    }

    // BUSCAR
    @Test
    void buscarPorCodigoCorrectamente() {
        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(crearSucursal()));

        SucursalDTO resultado
                = sucursalService.buscarPorCodigo("SUC00001");

        assertEquals("SUC00001", resultado.getCodigo());
        assertEquals("Av. Siempre Viva 123", resultado.getDomicilio());
        assertEquals(10, resultado.getCantEmpleados());
    }

    @Test
    void buscarPorCodigoInexistente() {
        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        sucursalService.buscarPorCodigo("SUC00001");
                    }
                }
        );
    }

    @Test
    void buscarPorDomicilioSinFiltroDeEstado() {
        List<Sucursal> sucursales = new ArrayList<>();
        sucursales.add(crearSucursal());

        when(sucursalRepository
                .findByDomicilioContainingIgnoreCase(anyString()))
                .thenReturn(sucursales);

        List<SucursalDTO> resultado
                = sucursalService.buscarPorDomicilio("Siempre Viva", null);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorDomicilioConFiltroDeEstado() {
        List<Sucursal> sucursales = new ArrayList<>();
        sucursales.add(crearSucursal());

        when(sucursalRepository
                .findByDomicilioContainingIgnoreCaseAndActivo(anyString(), eq(true)))
                .thenReturn(sucursales);

        List<SucursalDTO> resultado
                = sucursalService.buscarPorDomicilio("Siempre Viva", true);

        assertEquals(1, resultado.size());

        verify(sucursalRepository)
                .findByDomicilioContainingIgnoreCaseAndActivo(anyString(), eq(true));
    }

    // LISTAR POR ENTIDAD BANCARIA
    @Test
    void listarPorEntidadBancariaCorrectamente() {
        List<Sucursal> sucursales = new ArrayList<>();
        sucursales.add(crearSucursal());

        when(entidadBancariaRepository.findByCodigo("ENB00001"))
                .thenReturn(Optional.of(crearEntidad()));

        when(sucursalRepository.findByEntidadBancaria_Id(1L))
                .thenReturn(sucursales);

        List<SucursalDTO> resultado
                = sucursalService.listarPorEntidadBancaria("ENB00001");

        assertEquals(1, resultado.size());
        assertEquals("SUC00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarPorEntidadBancariaInexistente() {
        when(entidadBancariaRepository.findByCodigo("ENB00001"))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        sucursalService.listarPorEntidadBancaria("ENB00001");
                    }
                }
        );
    }

    // GUARDAR
    @Test
    void guardarSucursalCorrectamente() {
        configurarEntidadActiva();

        when(sucursalRepository.save(any(Sucursal.class)))
                .thenAnswer(new org.mockito.stubbing.Answer<Sucursal>() {
                    @Override
                    public Sucursal answer(
                            org.mockito.invocation.InvocationOnMock invocation) {

                        Sucursal sucursal = invocation.getArgument(0);

                        if (sucursal.getId() == null) {
                            sucursal.setId(1L);
                        }

                        return sucursal;
                    }
                });

        SucursalDTO resultado = sucursalService.guardar(crearDTO());

        assertNotNull(resultado);
        assertNotNull(resultado.getCodigo());
        assertTrue(resultado.getActivo());
        assertEquals("ENB00001", resultado.getEntidadBancariaCodigo());

        verify(sucursalRepository, times(2))
                .save(any(Sucursal.class));
    }

    @Test
    void guardarSucursalConEntidadInexistente() {
        when(entidadBancariaRepository.findByCodigo("ENB00001"))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        sucursalService.guardar(crearDTO());
                    }
                }
        );

        verify(sucursalRepository, never()).save(any(Sucursal.class));
    }

    @Test
    void guardarSucursalConEntidadInactiva() {
        EntidadBancaria entidad = crearEntidad();
        entidad.setActivo(false);

        when(entidadBancariaRepository.findByCodigo("ENB00001"))
                .thenReturn(Optional.of(entidad));

        assertThrows(
                EntidadInactivaException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        sucursalService.guardar(crearDTO());
                    }
                }
        );
    }

    // ACTUALIZAR
    @Test
    void actualizarSucursalCorrectamente() {
        Sucursal sucursal = crearSucursal();

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(sucursal));

        when(sucursalRepository.save(any(Sucursal.class)))
                .thenReturn(sucursal);

        SucursalDTO dto = new SucursalDTO();
        dto.setDomicilio("Nueva Direccion 456");
        dto.setCantEmpleados(20);

        SucursalDTO resultado
                = sucursalService.actualizar("SUC00001", dto);

        assertEquals(20, resultado.getCantEmpleados());
        assertEquals("Nueva Direccion 456", resultado.getDomicilio());
        verify(sucursalRepository).save(sucursal);
    }

    @Test
    void actualizarSucursalInexistente() {
        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        sucursalService.actualizar("SUC00001", new SucursalDTO());
                    }
                }
        );
    }

    @Test
    void actualizarSucursalCerrada() {
        Sucursal sucursal = crearSucursal();
        sucursal.setActivo(false);

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(sucursal));

        assertThrows(
                SucursalYaCerradaException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        sucursalService.actualizar("SUC00001", new SucursalDTO());
                    }
                }
        );

        verify(sucursalRepository, never()).save(any(Sucursal.class));
    }

    @Test
    void actualizarConEntidadInactiva() {
        Sucursal sucursal = crearSucursal();
        EntidadBancaria entidad = crearEntidad();
        entidad.setActivo(false);

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(sucursal));

        when(entidadBancariaRepository.findByCodigo("ENB00001"))
                .thenReturn(Optional.of(entidad));

        SucursalDTO dto = new SucursalDTO();
        dto.setEntidadBancariaCodigo("ENB00001");

        assertThrows(
                EntidadInactivaException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        sucursalService.actualizar("SUC00001", dto);
                    }
                }
        );
    }

    // ELIMINAR
    @Test
    void eliminarSucursalCorrectamente() {
        Sucursal sucursal = crearSucursal();

        ContratoVigilancia contrato = crearContrato(sucursal);

        List<ContratoVigilancia> contratos = new ArrayList<>();
        contratos.add(contrato);

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(sucursal));

        when(contratoRepository
                .findBySucursal_IdAndFechaAfterAndActivoTrue(
                        1L, LocalDate.now()))
                .thenReturn(contratos);

        SucursalDTO dto = new SucursalDTO();
        dto.setMotivoCierre(MotivoCierreSucursal.CIERRE_TEMPORAL);

        sucursalService.eliminar("SUC00001", dto);

        assertFalse(sucursal.isActivo());
        assertEquals(
                MotivoCierreSucursal.CIERRE_TEMPORAL,
                sucursal.getMotivoCierre()
        );

        assertFalse(contrato.getActivo());
        assertEquals(
                MotivoBajaContrato.CIERRE_SUCURSAL,
                contrato.getMotivoBaja()
        );

        verify(contratoRepository).saveAll(contratos);
        verify(sucursalRepository).save(sucursal);
    }

    @Test
    void eliminarSucursalSinMotivoDeCierre() {
        Sucursal sucursal = crearSucursal();

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(sucursal));

        assertThrows(
                MotivoCierreSucursalObligatorioException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        sucursalService.eliminar("SUC00001", new SucursalDTO());
                    }
                }
        );

        verify(contratoRepository, never()).saveAll(any());
        verify(sucursalRepository, never()).save(any(Sucursal.class));
    }

    @Test
    void eliminarSucursalYaCerrada() {
        Sucursal sucursal = crearSucursal();
        sucursal.setActivo(false);

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(sucursal));

        SucursalDTO dto = new SucursalDTO();
        dto.setMotivoCierre(MotivoCierreSucursal.MUDANZA);

        assertThrows(
                SucursalYaCerradaException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        sucursalService.eliminar("SUC00001", dto);
                    }
                }
        );
    }

    @Test
    void eliminarSucursalInexistente() {
        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        sucursalService.eliminar("SUC00001", new SucursalDTO());
                    }
                }
        );
    }

    // REACTIVAR
    @Test
    void reactivarSucursalCorrectamente() {
        Sucursal sucursal = crearSucursal();
        sucursal.setActivo(false);
        sucursal.setMotivoCierre(MotivoCierreSucursal.CIERRE_TEMPORAL);

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(sucursal));

        when(sucursalRepository.save(any(Sucursal.class)))
                .thenReturn(sucursal);

        SucursalDTO resultado
                = sucursalService.reactivar("SUC00001");

        assertTrue(resultado.getActivo());
        assertNull(resultado.getMotivoCierre());
        verify(sucursalRepository).save(sucursal);
    }

    @Test
    void reactivarSucursalYaActiva() {
        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(crearSucursal()));

        assertThrows(
                SucursalYaActivaException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        sucursalService.reactivar("SUC00001");
                    }
                }
        );
    }

    @Test
    void reactivarSucursalConCierreDefinitivo() {
        Sucursal sucursal = crearSucursal();
        sucursal.setActivo(false);
        sucursal.setMotivoCierre(MotivoCierreSucursal.CIERRE_DEFINITIVO);

        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.of(sucursal));

        assertThrows(
                SucursalNoReactivableException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        sucursalService.reactivar("SUC00001");
                    }
                }
        );

        verify(sucursalRepository, never()).save(any(Sucursal.class));
    }

    @Test
    void reactivarSucursalInexistente() {
        when(sucursalRepository.findByCodigo("SUC00001"))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        sucursalService.reactivar("SUC00001");
                    }
                }
        );
    }
}
