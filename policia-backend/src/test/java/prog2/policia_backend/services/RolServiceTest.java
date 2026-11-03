package prog2.policia_backend.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import prog2.policia_backend.DTOs.RolDTO;
import prog2.policia_backend.DTOs.RolPermisoEntidadDTO;
import prog2.policia_backend.exceptions.*;
import prog2.policia_backend.models.Permiso;
import prog2.policia_backend.models.Entidad;
import prog2.policia_backend.models.RolPermisoEntidad;
import prog2.policia_backend.models.Rol;
import prog2.policia_backend.repositories.RolRepository;
import prog2.policia_backend.repositories.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RolServiceTest {

    private RolRepository rolRepository;
    private RolService rolService;
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        rolRepository = mock(RolRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        rolService = new RolService(rolRepository, usuarioRepository);
    }

    private Set<RolPermisoEntidad> crearPermisos(Rol rol) {
        Set<RolPermisoEntidad> permisos = new HashSet<>();
        RolPermisoEntidad lectura = new RolPermisoEntidad();
        lectura.setRol(rol);
        lectura.setPermiso(Permiso.LECTURA);
        lectura.setEntidad(Entidad.USUARIO);
        permisos.add(lectura);
        RolPermisoEntidad escritura = new RolPermisoEntidad();
        escritura.setRol(rol);
        escritura.setPermiso(Permiso.ESCRITURA);
        escritura.setEntidad(Entidad.USUARIO);
        permisos.add(escritura);
        return permisos;
    }

    private Set<RolPermisoEntidadDTO> crearPermisosDTO() {
        Set<RolPermisoEntidadDTO> permisos = new HashSet<>();
        permisos.add(new RolPermisoEntidadDTO(Permiso.LECTURA, Entidad.USUARIO));
        permisos.add(new RolPermisoEntidadDTO(Permiso.ESCRITURA, Entidad.USUARIO));
        return permisos;
    }

    private Rol crearRol() {
        Rol rol = new Rol();

        rol.setId(1L);
        rol.setCodigo("ROL00001");
        rol.setNombre("SUPERVISOR");
        rol.setPrefijo("SUP");
        rol.setDescripcion("Supervisor del sistema");
        rol.setPermisosPorEntidad(crearPermisos(rol));
        rol.setActivo(true);

        return rol;
    }

    private RolDTO crearDTO() {
        RolDTO dto = new RolDTO();

        dto.setNombre("OPERADOR");
        dto.setPrefijo("OPE");
        dto.setDescripcion("Rol de operador");
        dto.setPermisosPorEntidad(crearPermisosDTO());

        return dto;
    }

    private void configurarGuardado() {
        when(rolRepository.save(any(Rol.class)))
                .thenAnswer(new org.mockito.stubbing.Answer<Rol>() {
                    @Override
                    public Rol answer(
                            org.mockito.invocation.InvocationOnMock invocation) {

                        Rol rol = invocation.getArgument(0);

                        if (rol.getId() == null) {
                            rol.setId(1L);
                        }

                        return rol;
                    }
                });
    }

    @Test
    void listarTodosLosRoles() {
        List<Rol> roles = new ArrayList<>();
        roles.add(crearRol());

        when(rolRepository.findAll()).thenReturn(roles);

        List<RolDTO> resultado = rolService.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("ROL00001", resultado.get(0).getCodigo());
        assertEquals("SUPERVISOR", resultado.get(0).getNombre());
    }

    @Test
    void listarRolesActivos() {
        List<Rol> roles = new ArrayList<>();
        roles.add(crearRol());

        when(rolRepository.findByActivo(true)).thenReturn(roles);

        List<RolDTO> resultado = rolService.listar(true);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());

        verify(rolRepository).findByActivo(true);
    }

    @Test
    void listarRolesInactivos() {
        when(rolRepository.findByActivo(false))
                .thenReturn(new ArrayList<Rol>());

        List<RolDTO> resultado = rolService.listar(false);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorCodigoCorrectamente() {
        when(rolRepository.findByCodigo("ROL00001"))
                .thenReturn(Optional.of(crearRol()));

        RolDTO resultado = rolService.buscarPorCodigo(" rol00001 ");

        assertEquals("ROL00001", resultado.getCodigo());
        assertEquals("SUPERVISOR", resultado.getNombre());

        verify(rolRepository).findByCodigo("ROL00001");
    }

    @Test
    void buscarPorCodigoInexistente() {
        when(rolRepository.findByCodigo("ROL00001"))
                .thenReturn(Optional.empty());

        try {
            rolService.buscarPorCodigo("ROL00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    @Test
    void guardarRolCorrectamente() {
        when(rolRepository.existsByNombre("OPERADOR"))
                .thenReturn(false);

        configurarGuardado();

        RolDTO resultado = rolService.guardar(crearDTO());

        assertNotNull(resultado);
        assertEquals("ROL00001", resultado.getCodigo());
        assertEquals("OPERADOR", resultado.getNombre());
        assertEquals("OPE", resultado.getPrefijo());
        assertTrue(resultado.getActivo());
        assertNotNull(resultado.getPermisosPorEntidad());
        assertEquals(2, resultado.getPermisosPorEntidad().size());

        verify(rolRepository, times(2)).save(any(Rol.class));
    }

    @Test
    void guardarNormalizaNombreYPrefijo() {
        RolDTO dto = crearDTO();
        dto.setNombre("  operador administrativo ");
        dto.setPrefijo(" ope ");

        when(rolRepository.existsByNombre("OPERADOR ADMINISTRATIVO"))
                .thenReturn(false);

        configurarGuardado();

        RolDTO resultado = rolService.guardar(dto);

        assertEquals("OPERADOR ADMINISTRATIVO", resultado.getNombre());
        assertEquals("OPE", resultado.getPrefijo());
    }

    @Test
    void guardarRechazaNombreExistente() {
        when(rolRepository.existsByNombre("OPERADOR"))
                .thenReturn(true);

        try {
            rolService.guardar(crearDTO());
            fail("Debía lanzar RolExistenteException");
        } catch (RolExistenteException e) {
            assertNotNull(e);
        }

        verify(rolRepository, never()).save(any(Rol.class));
    }

    @Test
    void guardarGeneraCodigoAutomaticamente() {
        when(rolRepository.existsByNombre("OPERADOR"))
                .thenReturn(false);

        configurarGuardado();

        RolDTO dto = crearDTO();
        dto.setCodigo(null);

        RolDTO resultado = rolService.guardar(dto);

        assertEquals("ROL00001", resultado.getCodigo());
    }

    @Test
    void actualizarNombreCorrectamente() {
        Rol rol = crearRol();

        when(rolRepository.findByCodigo("ROL00001"))
                .thenReturn(Optional.of(rol));

        when(rolRepository.existsByNombre("OPERADOR"))
                .thenReturn(false);

        when(rolRepository.save(any(Rol.class)))
                .thenReturn(rol);

        RolDTO dto = new RolDTO();
        dto.setNombre("OPERADOR");

        RolDTO resultado = rolService.actualizar("ROL00001", dto);

        assertEquals("OPERADOR", resultado.getNombre());
        verify(rolRepository).save(rol);
    }

    @Test
    void actualizarDescripcionCorrectamente() {
        Rol rol = crearRol();

        when(rolRepository.findByCodigo("ROL00001"))
                .thenReturn(Optional.of(rol));

        when(rolRepository.save(any(Rol.class)))
                .thenReturn(rol);

        RolDTO dto = new RolDTO();
        dto.setDescripcion(" nuevo rol administrativo ");

        RolDTO resultado = rolService.actualizar("ROL00001", dto);

        assertEquals("Nuevo Rol Administrativo", resultado.getDescripcion());
    }

    @Test
    void actualizarPrefijoCorrectamente() {
        Rol rol = crearRol();

        when(rolRepository.findByCodigo("ROL00001"))
                .thenReturn(Optional.of(rol));

        when(rolRepository.save(any(Rol.class)))
                .thenReturn(rol);

        RolDTO dto = new RolDTO();
        dto.setPrefijo(" ope ");

        RolDTO resultado = rolService.actualizar("ROL00001", dto);

        assertEquals("OPE", resultado.getPrefijo());
    }

    @Test
    void actualizarPermisosCorrectamente() {
        Rol rol = crearRol();

        when(rolRepository.findByCodigo("ROL00001"))
                .thenReturn(Optional.of(rol));

        when(rolRepository.save(any(Rol.class)))
                .thenReturn(rol);

        Set<RolPermisoEntidadDTO> nuevosPermisos = new HashSet<>();
        nuevosPermisos.add(new RolPermisoEntidadDTO(Permiso.LECTURA_DATOS_PROPIOS, Entidad.USUARIO));

        RolDTO dto = new RolDTO();
        dto.setPermisosPorEntidad(nuevosPermisos);

        RolDTO resultado = rolService.actualizar("ROL00001", dto);

        assertNotNull(resultado.getPermisosPorEntidad());
        assertEquals(1, resultado.getPermisosPorEntidad().size());
        RolPermisoEntidadDTO permiso = resultado.getPermisosPorEntidad().iterator().next();
        assertEquals(Permiso.LECTURA_DATOS_PROPIOS, permiso.getPermiso());
        assertEquals(Entidad.USUARIO, permiso.getEntidad());
    }

    @Test
    void actualizarRechazaNombreExistente() {
        Rol rol = crearRol();

        when(rolRepository.findByCodigo("ROL00001"))
                .thenReturn(Optional.of(rol));

        when(rolRepository.existsByNombre("OPERADOR"))
                .thenReturn(true);

        RolDTO dto = new RolDTO();
        dto.setNombre("OPERADOR");

        try {
            rolService.actualizar("ROL00001", dto);
            fail("Debía lanzar RolExistenteException");
        } catch (RolExistenteException e) {
            assertNotNull(e);
        }

        verify(rolRepository, never()).save(any(Rol.class));
    }

    @Test
    void actualizarNombreSinCambiosNoVerificaDuplicados() {
        Rol rol = crearRol();

        when(rolRepository.findByCodigo("ROL00001"))
                .thenReturn(Optional.of(rol));

        when(rolRepository.save(any(Rol.class)))
                .thenReturn(rol);

        RolDTO dto = new RolDTO();
        dto.setNombre("SUPERVISOR");

        rolService.actualizar("ROL00001", dto);

        verify(rolRepository, never()).existsByNombre(any());
    }

    @Test
    void actualizarRolInexistente() {
        when(rolRepository.findByCodigo("ROL00001"))
                .thenReturn(Optional.empty());

        try {
            rolService.actualizar("ROL00001", new RolDTO());
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }

    @Test
    void eliminarRolCorrectamente() {
        Rol rol = crearRol();

        when(rolRepository.findByCodigo("ROL00001"))
                .thenReturn(Optional.of(rol));

        when(rolRepository.save(any(Rol.class)))
                .thenReturn(rol);

        RolDTO resultado = rolService.eliminar("ROL00001");

        assertFalse(rol.getActivo());
        assertFalse(resultado.getActivo());

        verify(rolRepository).save(rol);
    }

    @Test
    void eliminarRolYaInactivo() {
        Rol rol = crearRol();
        rol.setActivo(false);

        when(rolRepository.findByCodigo("ROL00001"))
                .thenReturn(Optional.of(rol));

        try {
            rolService.eliminar("ROL00001");
            fail("Debía lanzar RolYaInactivoException");
        } catch (RolYaInactivoException e) {
            assertNotNull(e);
        }

        verify(rolRepository, never()).save(any(Rol.class));
    }

    @Test
    void reactivarRolCorrectamente() {
        Rol rol = crearRol();
        rol.setActivo(false);

        when(rolRepository.findByCodigo("ROL00001"))
                .thenReturn(Optional.of(rol));

        when(rolRepository.save(any(Rol.class)))
                .thenReturn(rol);

        RolDTO resultado = rolService.reactivar("ROL00001");

        assertTrue(rol.getActivo());
        assertTrue(resultado.getActivo());

        verify(rolRepository).save(rol);
    }

    @Test
    void reactivarRolYaActivo() {
        when(rolRepository.findByCodigo("ROL00001"))
                .thenReturn(Optional.of(crearRol()));

        try {
            rolService.reactivar("ROL00001");
            fail("Debía lanzar RolYaActivoException");
        } catch (RolYaActivoException e) {
            assertNotNull(e);
        }

        verify(rolRepository, never()).save(any(Rol.class));
    }

    @Test
    void reactivarRolInexistente() {
        when(rolRepository.findByCodigo("ROL00001"))
                .thenReturn(Optional.empty());

        try {
            rolService.reactivar("ROL00001");
            fail("Debía lanzar RecursoNoEncontradoException");
        } catch (RecursoNoEncontradoException e) {
            assertNotNull(e);
        }
    }
}
