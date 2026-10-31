package prog2.policia_backend.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.crypto.password.PasswordEncoder;

import prog2.policia_backend.DTOs.VigilanteDTO;
import prog2.policia_backend.exceptions.*;
import prog2.policia_backend.models.*;
import prog2.policia_backend.repositories.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VigilanteServiceTest {

    private VigilanteRepository vigilanteRepository;
    private PasswordEncoder passwordEncoder;
    private ContratoVigilanciaRepository contratoVigilanciaRepository;
    private RolRepository rolRepository;
    private VigilanteService vigilanteService;

    @BeforeEach
    void setUp() {
        vigilanteRepository = mock(VigilanteRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        contratoVigilanciaRepository = mock(ContratoVigilanciaRepository.class);
        rolRepository = mock(RolRepository.class);

        vigilanteService = new VigilanteService(
                vigilanteRepository,
                passwordEncoder,
                contratoVigilanciaRepository,
                rolRepository
        );
    }

    private Rol crearRol() {
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre("VIGILANTE");
        rol.setPrefijo("VIG");
        rol.setActivo(true);
        return rol;
    }

    private Vigilante crearVigilante(String codigo, String nombre, int edad, boolean activo) {
        Vigilante vigilante = new Vigilante();

        vigilante.setId(1L);
        vigilante.setCodigo(codigo);
        vigilante.setNombre(nombre);
        vigilante.setEdad(edad);
        vigilante.setPassword("encoded");
        vigilante.setRol(crearRol());
        vigilante.setActivo(activo);

        return vigilante;
    }

    private VigilanteDTO crearDTO(String nombre, Integer edad, String password) {
        VigilanteDTO dto = new VigilanteDTO();

        dto.setNombre(nombre);
        dto.setEdad(edad);
        dto.setPassword(password);

        return dto;
    }

    private void configurarGuardado() {
        when(vigilanteRepository.save(any(Vigilante.class)))
                .thenAnswer(new org.mockito.stubbing.Answer<Vigilante>() {
                    @Override
                    public Vigilante answer(
                            org.mockito.invocation.InvocationOnMock invocation) {
                        return invocation.getArgument(0);
                    }
                });
    }

    // LISTAR
    @Test
    void listarTodosLosVigilantes() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        List<Vigilante> vigilantes = new ArrayList<>();
        vigilantes.add(vigilante);

        when(vigilanteRepository.findAll()).thenReturn(vigilantes);

        List<VigilanteDTO> resultado = vigilanteService.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("VIG00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarVigilantesActivos() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        List<Vigilante> vigilantes = new ArrayList<>();
        vigilantes.add(vigilante);

        when(vigilanteRepository.findByActivo(true))
                .thenReturn(vigilantes);

        List<VigilanteDTO> resultado = vigilanteService.listar(true);

        assertEquals(1, resultado.size());
        verify(vigilanteRepository).findByActivo(true);
    }

    @Test
    void listarVigilantesInactivos() {
        when(vigilanteRepository.findByActivo(false))
                .thenReturn(new ArrayList<Vigilante>());

        List<VigilanteDTO> resultado = vigilanteService.listar(false);

        assertTrue(resultado.isEmpty());
    }

    // BUSCAR POR CODIGO
    @Test
    void buscarPorCodigoEncuentraVigilante() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        VigilanteDTO resultado = vigilanteService.buscarPorCodigo(" vig00001 ");

        assertEquals("VIG00001", resultado.getCodigo());
        assertEquals("Juan Perez", resultado.getNombre());
    }

    @Test
    void buscarPorCodigoInexistenteLanzaExcepcion() {
        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.empty());

        try {
            vigilanteService.buscarPorCodigo("VIG00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // BUSCAR POR NOMBRE
    @Test
    void buscarPorNombreEncuentraCoincidencias() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        List<Vigilante> vigilantes = new ArrayList<>();
        vigilantes.add(vigilante);

        when(vigilanteRepository.findAll()).thenReturn(vigilantes);

        List<VigilanteDTO> resultado
                = vigilanteService.buscarPorNombre("juan", null);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorNombreRespetaFiltroActivo() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        List<Vigilante> vigilantes = new ArrayList<>();
        vigilantes.add(vigilante);

        when(vigilanteRepository.findAll()).thenReturn(vigilantes);

        List<VigilanteDTO> resultado
                = vigilanteService.buscarPorNombre("Juan", false);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorNombreSinCoincidencias() {
        when(vigilanteRepository.findAll())
                .thenReturn(new ArrayList<Vigilante>());

        List<VigilanteDTO> resultado
                = vigilanteService.buscarPorNombre("Pedro", null);

        assertTrue(resultado.isEmpty());
    }

    // GUARDAR
    @Test
    void guardarVigilanteCorrectamente() {
        Rol rol = crearRol();

        when(rolRepository.findByNombre("VIGILANTE"))
                .thenReturn(Optional.of(rol));

        when(passwordEncoder.encode("clave123"))
                .thenReturn("encoded");

        when(vigilanteRepository.existsByCodigo("VIG00001"))
                .thenReturn(false);

        configurarGuardado();

        VigilanteDTO dto = crearDTO("Juan Perez", 30, "clave123");

        VigilanteDTO resultado = vigilanteService.guardar(dto);

        assertNotNull(resultado);
        assertEquals("VIG00001", resultado.getCodigo());
        assertEquals("Juan Perez", resultado.getNombre());
        assertEquals(30, resultado.getEdad());
        assertEquals("VIGILANTE", resultado.getRol());
        assertTrue(resultado.getActivo());
        assertNull(resultado.getPassword());

        verify(vigilanteRepository, times(2))
                .save(any(Vigilante.class));

        verify(passwordEncoder).encode("clave123");
    }

    @Test
    void guardarRechazaEdadMenorA18() {
        VigilanteDTO dto = crearDTO("Juan Perez", 17, "clave123");

        try {
            vigilanteService.guardar(dto);
            fail("Debía lanzar EdadInvalidaException");
        } catch (EdadInvalidaException e) {
            assertNotNull(e);
        }

        verifyNoInteractions(rolRepository);
        verifyNoInteractions(vigilanteRepository);
    }

    @Test
    void guardarRechazaEdadMayorA65() {
        VigilanteDTO dto = crearDTO("Juan Perez", 66, "clave123");

        try {
            vigilanteService.guardar(dto);
            fail("Debía lanzar EdadInvalidaException");
        } catch (EdadInvalidaException e) {
            assertNotNull(e);
        }
    }

    @Test
    void guardarRechazaEdadNula() {
        VigilanteDTO dto = crearDTO("Juan Perez", null, "clave123");

        try {
            vigilanteService.guardar(dto);
            fail("Debía lanzar EdadInvalidaException");
        } catch (EdadInvalidaException e) {
            assertNotNull(e);
        }
    }

    @Test
    void guardarRechazaRolInexistente() {
        when(rolRepository.findByNombre("VIGILANTE"))
                .thenReturn(Optional.empty());

        try {
            vigilanteService.guardar(
                    crearDTO("Juan Perez", 30, "clave123")
            );
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // ACTUALIZAR
    @Test
    void actualizarNombreCorrectamente() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        when(vigilanteRepository.save(any(Vigilante.class)))
                .thenReturn(vigilante);

        VigilanteDTO dto = new VigilanteDTO();
        dto.setNombre("Carlos Gomez");

        VigilanteDTO resultado
                = vigilanteService.actualizar("VIG00001", dto);

        assertEquals("Carlos Gomez", resultado.getNombre());
    }

    @Test
    void actualizarEdadCorrectamente() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        when(vigilanteRepository.save(any(Vigilante.class)))
                .thenReturn(vigilante);

        VigilanteDTO dto = new VigilanteDTO();
        dto.setEdad(40);

        VigilanteDTO resultado
                = vigilanteService.actualizar("VIG00001", dto);

        assertEquals(40, resultado.getEdad());
    }

    @Test
    void actualizarPasswordCorrectamente() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        when(passwordEncoder.encode("nueva123"))
                .thenReturn("nuevaEncoded");

        when(vigilanteRepository.save(any(Vigilante.class)))
                .thenReturn(vigilante);

        VigilanteDTO dto = new VigilanteDTO();
        dto.setPassword("nueva123");

        vigilanteService.actualizar("VIG00001", dto);

        assertEquals("nuevaEncoded", vigilante.getPassword());
        verify(passwordEncoder).encode("nueva123");
    }

    @Test
    void actualizarRechazaVigilanteInactivo() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, false
        );

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        try {
            vigilanteService.actualizar(
                    "VIG00001", new VigilanteDTO()
            );
            fail("Debía lanzar PersonaInactivaException");
        } catch (PersonaInactivaException e) {
            assertNotNull(e);
        }

        verify(vigilanteRepository, never())
                .save(any(Vigilante.class));
    }

    @Test
    void actualizarRechazaEdadInvalida() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        VigilanteDTO dto = new VigilanteDTO();
        dto.setEdad(70);

        try {
            vigilanteService.actualizar("VIG00001", dto);
            fail("Debía lanzar EdadInvalidaException");
        } catch (EdadInvalidaException e) {
            assertNotNull(e);
        }
    }

    // LISTAR PROPIO
    @Test
    void listarPropioDevuelveSusDatos() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        List<VigilanteDTO> resultado
                = vigilanteService.listarPropio("VIG00001", null, null);

        assertEquals(1, resultado.size());
        assertEquals("VIG00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarPropioRespetaFiltroActivo() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        List<VigilanteDTO> resultado
                = vigilanteService.listarPropio("VIG00001", false, null);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void listarPropioRespetaFiltroNombre() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        List<VigilanteDTO> resultado
                = vigilanteService.listarPropio("VIG00001", null, "Pedro");

        assertTrue(resultado.isEmpty());
    }

    // ELIMINAR
    @Test
    void eliminarVigilanteCorrectamente() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        when(contratoVigilanciaRepository
                .existsByVigilante_IdAndFechaGreaterThanEqualAndActivoTrue(
                        eq(1L), any(LocalDate.class)))
                .thenReturn(false);

        VigilanteDTO dto = new VigilanteDTO();
        dto.setMotivoBaja(MotivoBajaPersona.RENUNCIA);

        vigilanteService.eliminar("VIG00001", dto);

        assertFalse(vigilante.isActivo());
        assertEquals(
                MotivoBajaPersona.RENUNCIA,
                vigilante.getMotivoBaja()
        );

        verify(vigilanteRepository).save(vigilante);
    }

    @Test
    void eliminarRechazaVigilanteInactivo() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, false
        );

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        try {
            vigilanteService.eliminar("VIG00001", new VigilanteDTO());
            fail("Debía lanzar PersonaYaInactivaException");
        } catch (PersonaYaInactivaException e) {
            assertNotNull(e);
        }
    }

    @Test
    void eliminarExigeMotivoBaja() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        try {
            vigilanteService.eliminar("VIG00001", new VigilanteDTO());
            fail("Debía lanzar MotivoBajaObligatorioException");
        } catch (MotivoBajaObligatorioException e) {
            assertNotNull(e);
        }

        verifyNoInteractions(contratoVigilanciaRepository);
    }

    @Test
    void eliminarRechazaVigilanteConContratoFuturo() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        when(contratoVigilanciaRepository
                .existsByVigilante_IdAndFechaGreaterThanEqualAndActivoTrue(
                        eq(1L), any(LocalDate.class)))
                .thenReturn(true);

        VigilanteDTO dto = new VigilanteDTO();
        dto.setMotivoBaja(MotivoBajaPersona.RENUNCIA);

        try {
            vigilanteService.eliminar("VIG00001", dto);
            fail("Debía lanzar VigilanteConContratoFuturoException");
        } catch (VigilanteConContratoFuturoException e) {
            assertNotNull(e);
        }

        verify(vigilanteRepository, never())
                .save(any(Vigilante.class));
    }

    // REACTIVAR
    @Test
    void reactivarVigilanteCorrectamente() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, false
        );

        vigilante.setMotivoBaja(MotivoBajaPersona.RENUNCIA);

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        when(vigilanteRepository.save(any(Vigilante.class)))
                .thenReturn(vigilante);

        VigilanteDTO resultado
                = vigilanteService.reactivar("VIG00001");

        assertTrue(vigilante.isActivo());
        assertNull(vigilante.getMotivoBaja());
        assertEquals("VIG00001", resultado.getCodigo());
    }

    @Test
    void reactivarRechazaVigilanteYaActivo() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, true
        );

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        try {
            vigilanteService.reactivar("VIG00001");
            fail("Debía lanzar PersonaYaActivaException");
        } catch (PersonaYaActivaException e) {
            assertNotNull(e);
        }
    }

    @Test
    void reactivarRechazaVigilanteFallecido() {
        Vigilante vigilante = crearVigilante(
                "VIG00001", "Juan Perez", 30, false
        );

        vigilante.setMotivoBaja(MotivoBajaPersona.FALLECIMIENTO);

        when(vigilanteRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        try {
            vigilanteService.reactivar("VIG00001");
            fail("Debía lanzar PersonaNoReactivableException");
        } catch (PersonaNoReactivableException e) {
            assertNotNull(e);
        }
    }
}
