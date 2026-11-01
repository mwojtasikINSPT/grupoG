package prog2.policia_backend.services;

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
import prog2.policia_backend.repositories.UsuarioRepository;
import prog2.policia_backend.repositories.RolRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private RolRepository rolRepository;
    private PasswordEncoder passwordEncoder;
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        rolRepository = mock(RolRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        usuarioService = new UsuarioService(
                usuarioRepository,
                rolRepository,
                passwordEncoder
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

    private UsuarioGenerico crearUsuario(String codigo, String nombre, boolean activo) {
        UsuarioGenerico usuario = new UsuarioGenerico();

        usuario.setId(1L);
        usuario.setCodigo(codigo);
        usuario.setNombre(nombre);
        usuario.setPassword("encoded");
        usuario.setRol(crearRol("ADMINISTRADOR", "ADM"));
        usuario.setActivo(activo);

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
    void listarTodosLosUsuariosGenericos() {
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);

        Vigilante vigilante = new Vigilante();

        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(usuario);
        usuarios.add(vigilante);

        when(usuarioRepository.findAll()).thenReturn(usuarios);

        List<UsuarioDTO> resultado = usuarioService.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("ADM00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarUsuariosPorEstado() {
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);

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
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);

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
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        List<UsuarioDTO> resultado = usuarioService.listarPropio(
                "ADM00001", false, null
        );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void listarPropioFiltraPorNombre() {
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        List<UsuarioDTO> resultado = usuarioService.listarPropio(
                "ADM00001", null, "Juan"
        );

        assertEquals(1, resultado.size());
    }

    @Test
    void listarPropioNoDevuelveVigilantes() {
        Vigilante vigilante = new Vigilante();
        vigilante.setCodigo("VIG00001");

        when(usuarioRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        List<UsuarioDTO> resultado = usuarioService.listarPropio(
                "VIG00001", null, null
        );

        assertTrue(resultado.isEmpty());
    }

    // BUSCAR POR NOMBRE
    @Test
    void buscarPorNombreEncuentraCoincidencias() {
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);

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
    void buscarPorNombreExcluyeVigilantes() {
        Vigilante vigilante = new Vigilante();
        vigilante.setNombre("Juan Perez");

        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(vigilante);

        when(usuarioRepository.findByNombreContainingIgnoreCase(anyString())).thenReturn(usuarios);

        List<UsuarioDTO> resultado = usuarioService.buscarPorNombre("Juan", null);

        assertTrue(resultado.isEmpty());
    }

    // BUSCAR POR CODIGO
    @Test
    void buscarPorCodigoEncuentraUsuario() {
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        UsuarioDTO resultado = usuarioService.buscarPorCodigo(" adm00001 ");

        assertEquals("ADM00001", resultado.getCodigo());
        verify(usuarioRepository).findByCodigo("ADM00001");
    }

    @Test
    void buscarPorCodigoRechazaVigilantes() {
        Vigilante vigilante = new Vigilante();

        when(usuarioRepository.findByCodigo("VIG00001"))
                .thenReturn(Optional.of(vigilante));

        try {
            usuarioService.buscarPorCodigo("VIG00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
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
        UsuarioGenerico usuario = new UsuarioGenerico();

        usuario.setId(1L);
        usuario.setRol(rol);

        when(rolRepository.findByNombre("ADMINISTRADOR"))
                .thenReturn(Optional.of(rol));

        when(passwordEncoder.encode("clave123"))
                .thenReturn("encoded");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(new org.mockito.stubbing.Answer<Usuario>() {
                    @Override
                    public Usuario answer(org.mockito.invocation.InvocationOnMock invocation) {
                        return invocation.getArgument(0);
                    }
                });

        when(usuarioRepository.existsByCodigo(anyString()))
                .thenReturn(false);

        UsuarioDTO resultado = usuarioService.guardar(
                crearDTO("Juan Perez", "ADMINISTRADOR")
        );

        assertNotNull(resultado);
        assertNotNull(resultado.getCodigo());
        assertEquals("Juan Perez", resultado.getNombre());
        assertTrue(resultado.getActivo());

        verify(usuarioRepository, times(2)).save(any(Usuario.class));
        verify(passwordEncoder).encode("clave123");
    }

    @Test
    void guardarRechazaRolVigilante() {
        try {
            usuarioService.guardar(crearDTO("Juan Perez", "VIGILANTE"));
            fail("Debía lanzar OperacionNoPermitidaException");
        } catch (OperacionNoPermitidaException e) {
            assertNotNull(e);
        }

        verifyNoInteractions(usuarioRepository);
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
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);

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
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);

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
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", false);

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
    void actualizarRechazaRolVigilante() {
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);

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
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);

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
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);

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
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", false);

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
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);

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
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", false);
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
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);

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
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", false);
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
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);
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
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", true);
        autenticar("ADM00001");

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        CambioPasswordDTO dto = new CambioPasswordDTO();
        dto.setPasswordActual("incorrecta");
        dto.setPasswordNueva("nueva123");

        assertThrows(
                OperacionNoPermitidaException.class,
                () -> usuarioService.cambiarPassword(dto)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void cambiarPasswordRechazaUsuarioInactivo() {
        UsuarioGenerico usuario = crearUsuario("ADM00001", "Juan Perez", false);
        autenticar("ADM00001");

        when(usuarioRepository.findByCodigo("ADM00001"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("actual123", "encoded"))
                .thenReturn(true);

        CambioPasswordDTO dto = new CambioPasswordDTO();
        dto.setPasswordActual("actual123");
        dto.setPasswordNueva("nueva123");

        assertThrows(
                PersonaInactivaException.class,
                () -> usuarioService.cambiarPassword(dto)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void cambiarPasswordPermiteVigilantes() {
        Vigilante vigilante = new Vigilante();
        vigilante.setCodigo("VIG00001");
        vigilante.setPassword("encoded");
        vigilante.setActivo(true);

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
