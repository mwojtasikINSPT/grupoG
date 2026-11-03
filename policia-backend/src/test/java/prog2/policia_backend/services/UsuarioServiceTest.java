package prog2.policia_backend.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import prog2.policia_backend.DTOs.CambioPasswordDTO;
import prog2.policia_backend.DTOs.UsuarioDTO;
import prog2.policia_backend.exceptions.*;
import prog2.policia_backend.models.*;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;
import prog2.policia_backend.repositories.UsuarioRepository;
import prog2.policia_backend.repositories.RolRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private RolRepository rolRepository;
    private PasswordEncoder passwordEncoder;
    private UsuarioService usuarioService;
    private ContratoVigilanciaRepository contratoVigilanciaRepository;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        rolRepository = mock(RolRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        contratoVigilanciaRepository = mock(ContratoVigilanciaRepository.class);

        usuarioService = new UsuarioService(
                usuarioRepository,
                rolRepository,
                passwordEncoder,
                contratoVigilanciaRepository
        );
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private Rol crearRol(String nombre, String prefijo) {
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre(nombre);
        rol.setPrefijo(prefijo);
        rol.setActivo(true);
        return rol;
    }

    private Usuario crearUsuario(String codigo, String nombre, boolean activo) {
        Usuario usuario = new Usuario();

        usuario.setId(1L);
        usuario.setCodigo(codigo);
        usuario.setNombre(nombre);
        usuario.setPassword("encoded");
        usuario.setRol(crearRol("ADMINISTRADOR", "ADM"));
        usuario.setActivo(activo);
        usuario.setFechaNacimiento(LocalDate.now().minusYears(30));

        return usuario;
    }

    private UsuarioDTO crearDTO(String nombre, String rol) {
        UsuarioDTO dto = new UsuarioDTO();

        dto.setNombre(nombre);
        dto.setPassword("clave123");
        dto.setRol(rol);

        return dto;
    }

    private void autenticar(String codigo) {
        UsernamePasswordAuthenticationToken autenticacion
                = new UsernamePasswordAuthenticationToken(codigo, "password");

        SecurityContextHolder.getContext().setAuthentication(autenticacion);
    }

    // LISTAR
    @Test
    void listarTodosLosUsuarios() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);

        Usuario vigilante = new Usuario();
        vigilante.setId(2L);
        vigilante.setActivo(true);

        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(usuario);
        vigilante.setCodigo("VIG00001");
        vigilante.setNombre("Pedro Gomez");
        vigilante.setRol(crearRol("VIGILANTE", "VIG"));
        vigilante.setFechaNacimiento(LocalDate.now().minusYears(25));
        usuarios.add(vigilante);

        when(usuarioRepository.findAll()).thenReturn(usuarios);

        List<UsuarioDTO> resultado = usuarioService.listar(null);

        assertEquals(2, resultado.size());
        assertEquals("ADM00001", resultado.get(0).getCodigo());
        assertEquals("VIG00001", resultado.get(1).getCodigo());
    }

    @Test
    void listarUsuariosPorEstado() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);

        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(usuario);

        when(usuarioRepository.findByActivo(true)).thenReturn(usuarios);

        List<UsuarioDTO> resultado = usuarioService.listar(true);

        assertEquals(1, resultado.size());
        verify(usuarioRepository).findByActivo(true);
    }

    @Test
    void listarSinResultados() {
        when(usuarioRepository.findAll()).thenReturn(new ArrayList<Usuario>());

        List<UsuarioDTO> resultado = usuarioService.listar(null);

        assertTrue(resultado.isEmpty());
    }

    // LISTAR PROPIO
    @Test
    void listarPropioDevuelveSusDatos() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        List<UsuarioDTO> resultado = usuarioService.listarPropio(
                "ADM00001", null, null
        );

        assertEquals(1, resultado.size());
        assertEquals("ADM00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarPropioNoDevuelveUsuarioConEstadoDiferente() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        List<UsuarioDTO> resultado = usuarioService.listarPropio(
                "ADM00001", false, null
        );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void listarPropioFiltraPorNombre() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        List<UsuarioDTO> resultado = usuarioService.listarPropio(
                "ADM00001", null, "Juan"
        );

        assertEquals(1, resultado.size());
    }

    @Test
    void listarPropioPermiteVigilantes() {
        Usuario vigilante = new Usuario();
        vigilante.setCodigo("VIG00001");
        vigilante.setNombre("Pedro Gomez");
        vigilante.setRol(crearRol("VIGILANTE", "VIG"));
        vigilante.setFechaNacimiento(LocalDate.now().minusYears(25));

        when(usuarioRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        List<UsuarioDTO> resultado = usuarioService.listarPropio(
                "VIG00001", null, null
        );

        assertEquals(1, resultado.size());
        assertEquals("VIG00001", resultado.get(0).getCodigo());
    }

    // BUSCAR POR NOMBRE
    @Test
    void buscarPorNombreEncuentraCoincidencias() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);

        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(usuario);

        when(usuarioRepository.findByNombreContainingIgnoreCase(anyString())).thenReturn(usuarios);

        List<UsuarioDTO> resultado = usuarioService.buscarPorNombre("juan", null);

        assertEquals(1, resultado.size());
        assertEquals("Juan Perez", resultado.get(0).getNombre());
    }

    @Test
    void buscarPorNombreRespetaFiltroActivo() {
        when(usuarioRepository.findByNombreContainingIgnoreCaseAndActivo(anyString(), eq(false))).thenReturn(new ArrayList<>());

        List<UsuarioDTO> resultado = usuarioService.buscarPorNombre("Juan", false);

        assertTrue(resultado.isEmpty());

        verify(usuarioRepository).findByNombreContainingIgnoreCaseAndActivo(anyString(), eq(false));
    }

    @Test
    void buscarPorNombreIncluyeVigilantes() {
        Usuario vigilante = new Usuario();
        vigilante.setNombre("Juan Perez");
        vigilante.setCodigo("VIG00001");
        vigilante.setRol(crearRol("VIGILANTE", "VIG"));
        vigilante.setFechaNacimiento(LocalDate.now().minusYears(30));

        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(vigilante);

        when(usuarioRepository.findByNombreContainingIgnoreCase(anyString())).thenReturn(usuarios);

        List<UsuarioDTO> resultado = usuarioService.buscarPorNombre("Juan", null);

        assertEquals(1, resultado.size());
        assertEquals("VIG00001", resultado.get(0).getCodigo());
    }

    // BUSCAR POR CODIGO
    @Test
    void buscarPorCodigoEncuentraUsuario() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        UsuarioDTO resultado = usuarioService.buscarPorCodigo(" adm00001 ");

        assertEquals("ADM00001", resultado.getCodigo());
        verify(usuarioRepository).findByCodigo("ADM00001");
    }

    @Test
    void buscarPorCodigoPermiteVigilantes() {
        Usuario vigilante = crearUsuario("VIG00001", "Juan Perez", true);
        vigilante.setRol(crearRol("VIGILANTE", "VIG"));

        when(usuarioRepository.findByCodigo("VIG00001")).thenReturn(Optional.of(vigilante));

        UsuarioDTO resultado = usuarioService.buscarPorCodigo("VIG00001");

        assertEquals("VIG00001", resultado.getCodigo());
    }

    @Test
    void buscarPorCodigoInexistenteLanzaExcepcion() {
        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.empty());

        try {
            usuarioService.buscarPorCodigo("ADM00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // GUARDAR
    @Test
    void guardarUsuarioCorrectamente() {
        Rol rol = crearRol("ADMINISTRADOR", "ADM");
        when(rolRepository.findByNombre("ADMINISTRADOR"))
                .thenReturn(Optional.of(rol));

        when(passwordEncoder.encode("clave123"))
                .thenReturn("encoded");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(new org.mockito.stubbing.Answer<Usuario>() {
                    @Override
                    public Usuario answer(org.mockito.invocation.InvocationOnMock invocation) {
                        Usuario usuario = invocation.getArgument(0);
                        if (usuario.getId() == null) {
                            usuario.setId(1L);
                        }
                        return usuario;
                    }
                });

        when(usuarioRepository.existsByCodigo(anyString()))
                .thenReturn(false);

        UsuarioDTO dto = crearDTO("Juan Perez", "ADMINISTRADOR");
        dto.setFechaNacimiento(LocalDate.now().minusYears(30));

        UsuarioDTO resultado = usuarioService.guardar(dto);

        assertNotNull(resultado);
        assertNotNull(resultado.getCodigo());
        assertEquals("Juan Perez", resultado.getNombre());
        assertTrue(resultado.getActivo());
        assertEquals(30, resultado.getEdad());

        verify(usuarioRepository, times(2)).save(any(Usuario.class));
        verify(passwordEncoder).encode("clave123");
    }

    @Test
    void guardarPermiteRolVigilante() {
        Rol rol = crearRol("VIGILANTE", "VIG");
        when(rolRepository.findByNombre("VIGILANTE")).thenReturn(Optional.of(rol));
        when(passwordEncoder.encode("clave123")).thenReturn("encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(new org.mockito.stubbing.Answer<Usuario>() {
            @Override
            public Usuario answer(org.mockito.invocation.InvocationOnMock invocation) {
                Usuario usuario = invocation.getArgument(0);
                if (usuario.getId() == null) {
                    usuario.setId(1L);
                }
                return usuario;
            }
        });
        UsuarioDTO dto = crearDTO("Juan Perez", "VIGILANTE");
        dto.setFechaNacimiento(LocalDate.now().minusYears(30));

        UsuarioDTO resultado = usuarioService.guardar(dto);

        assertEquals("VIGILANTE", resultado.getRol());
        assertEquals("Juan Perez", resultado.getNombre());
        verify(usuarioRepository, times(2)).save(any(Usuario.class));
    }

    @Test
    void guardarRechazaRolInexistente() {
        when(rolRepository.findByNombre("ADMINISTRADOR"))
                .thenReturn(Optional.empty());

        try {
            usuarioService.guardar(crearDTO("Juan Perez", "ADMINISTRADOR"));
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    // ACTUALIZAR
    @Test
    void actualizarNombreCorrectamente() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuario);

        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombre("Carlos Gomez");

        UsuarioDTO resultado = usuarioService.actualizar("ADM00001", dto);

        assertEquals("Carlos Gomez", resultado.getNombre());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void actualizarContraseñaCorrectamente() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.encode("nueva123"))
                .thenReturn("nuevaEncoded");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuario);

        UsuarioDTO dto = new UsuarioDTO();
        dto.setPassword("nueva123");

        usuarioService.actualizar("ADM00001", dto);

        assertEquals("nuevaEncoded", usuario.getPassword());
        verify(passwordEncoder).encode("nueva123");
    }

    @Test
    void actualizarRechazaUsuarioInactivo() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", false);

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        try {
            usuarioService.actualizar("ADM00001", new UsuarioDTO());
            fail("Debía lanzar PersonaInactivaException");
        } catch (PersonaInactivaException e) {
            assertNotNull(e);
        }

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void actualizarRechazaCambioDeRol() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        UsuarioDTO dto = new UsuarioDTO();
        dto.setRol("VIGILANTE");

        try {
            usuarioService.actualizar("ADM00001", dto);
            fail("Debía lanzar OperacionNoPermitidaException");
        } catch (OperacionNoPermitidaException e) {
            assertNotNull(e);
        }
    }

    // ELIMINAR
    @Test
    void eliminarUsuarioCorrectamente() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);

        autenticar("ADM00002");

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        UsuarioDTO dto = new UsuarioDTO();
        dto.setMotivoBaja(MotivoBajaPersona.RENUNCIA);

        usuarioService.eliminar("ADM00001", dto);

        assertFalse(usuario.isActivo());
        assertEquals(MotivoBajaPersona.RENUNCIA, usuario.getMotivoBaja());

        verify(usuarioRepository).save(usuario);
    }

    @Test
    void eliminarExigeMotivoBaja() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);

        autenticar("ADM00002");

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        try {
            usuarioService.eliminar("ADM00001", new UsuarioDTO());
            fail("Debía lanzar MotivoBajaObligatorioException");
        } catch (MotivoBajaObligatorioException e) {
            assertNotNull(e);
        }

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void eliminarRechazaUsuarioYaInactivo() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", false);

        autenticar("ADM00002");

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        UsuarioDTO dto = new UsuarioDTO();
        dto.setMotivoBaja(MotivoBajaPersona.RENUNCIA);

        try {
            usuarioService.eliminar("ADM00001", dto);
            fail("Debía lanzar PersonaYaInactivaException");
        } catch (PersonaYaInactivaException e) {
            assertNotNull(e);
        }
    }

    @Test
    void administradorNoPuedeEliminarseASiMismo() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);

        autenticar("ADM00001");

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        UsuarioDTO dto = new UsuarioDTO();
        dto.setMotivoBaja(MotivoBajaPersona.RENUNCIA);

        try {
            usuarioService.eliminar("ADM00001", dto);
            fail("Debía lanzar OperacionNoPermitidaException");
        } catch (OperacionNoPermitidaException e) {
            assertNotNull(e);
        }

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // REACTIVAR
    @Test
    void reactivarUsuarioCorrectamente() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", false);
        usuario.setMotivoBaja(MotivoBajaPersona.RENUNCIA);

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuario);

        UsuarioDTO resultado = usuarioService.reactivar("ADM00001");

        assertTrue(usuario.isActivo());
        assertNull(usuario.getMotivoBaja());
        assertEquals("ADM00001", resultado.getCodigo());
    }

    @Test
    void reactivarRechazaUsuarioYaActivo() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        try {
            usuarioService.reactivar("ADM00001");
            fail("Debía lanzar PersonaYaActivaException");
        } catch (PersonaYaActivaException e) {
            assertNotNull(e);
        }
    }

    @Test
    void reactivarRechazaUsuarioFallecido() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", false);
        usuario.setMotivoBaja(MotivoBajaPersona.FALLECIMIENTO);

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        try {
            usuarioService.reactivar("ADM00001");
            fail("Debía lanzar PersonaNoReactivableException");
        } catch (PersonaNoReactivableException e) {
            assertNotNull(e);
        }
    }

    @Test
    void cambiarPasswordCorrectamente() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);
        autenticar("ADM00001");

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("actual123", "encoded"))
                .thenReturn(true);

        when(passwordEncoder.encode("nueva123"))
                .thenReturn("nuevaEncoded");

        CambioPasswordDTO dto = new CambioPasswordDTO();
        dto.setPasswordActual("actual123");
        dto.setPasswordNueva("nueva123");

        usuarioService.cambiarPassword(dto);

        assertEquals("nuevaEncoded", usuario.getPassword());
        verify(passwordEncoder).matches("actual123", "encoded");
        verify(passwordEncoder).encode("nueva123");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarPasswordRechazaPasswordActualIncorrecta() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", true);
        autenticar("ADM00001");

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        CambioPasswordDTO dto = new CambioPasswordDTO();
        dto.setPasswordActual("incorrecta");
        dto.setPasswordNueva("nueva123");

        try {
            usuarioService.cambiarPassword(dto);
            fail("Debía lanzar OperacionNoPermitidaException");
        } catch (OperacionNoPermitidaException e) {
            assertNotNull(e);
        }

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void cambiarPasswordRechazaUsuarioInactivo() {
        Usuario usuario = crearUsuario("ADM00001", "Juan Perez", false);
        autenticar("ADM00001");

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("actual123", "encoded"))
                .thenReturn(true);

        CambioPasswordDTO dto = new CambioPasswordDTO();
        dto.setPasswordActual("actual123");
        dto.setPasswordNueva("nueva123");

        try {
            usuarioService.cambiarPassword(dto);
            fail("Debía lanzar PersonaInactivaException");
        } catch (PersonaInactivaException e) {
            assertNotNull(e);
        }

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void cambiarPasswordPermiteVigilantes() {
        Usuario vigilante = new Usuario();
        vigilante.setCodigo("VIG00001");
        vigilante.setPassword("encoded");
        vigilante.setActivo(true);
        vigilante.setRol(crearRol("VIGILANTE", "VIG"));
        vigilante.setFechaNacimiento(LocalDate.now().minusYears(30));

        autenticar("VIG00001");

        when(usuarioRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        when(passwordEncoder.matches("actual123", "encoded"))
                .thenReturn(true);

        when(passwordEncoder.encode("nueva123"))
                .thenReturn("nuevaEncoded");

        CambioPasswordDTO dto = new CambioPasswordDTO();
        dto.setPasswordActual("actual123");
        dto.setPasswordNueva("nueva123");

        usuarioService.cambiarPassword(dto);

        assertEquals("nuevaEncoded", vigilante.getPassword());
        verify(usuarioRepository).save(vigilante);
    }
}
