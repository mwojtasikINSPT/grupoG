package prog2.policia_backend.services;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import prog2.policia_backend.DTOs.RolDTO;
import prog2.policia_backend.DTOs.RolPermisoEntidadDTO;
import prog2.policia_backend.exceptions.OperacionNoPermitidaException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Entidad;
import prog2.policia_backend.models.Permiso;
import prog2.policia_backend.models.Rol;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;
import prog2.policia_backend.repositories.RolRepository;
import prog2.policia_backend.repositories.UsuarioRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RolServiceTest {
    private RolRepository roles;
    private UsuarioRepository usuarios;
    private RolService servicio;

    @BeforeEach
    void setUp() {
        roles = mock(RolRepository.class);
        usuarios = mock(UsuarioRepository.class);
        AsaltanteRepository asaltantes = mock(AsaltanteRepository.class);
        ContratoVigilanciaRepository contratos = mock(ContratoVigilanciaRepository.class);
        servicio = new RolService(roles, usuarios, asaltantes, contratos);
    }

    @Test
    void buscarRolInexistente() {
        when(roles.findByCodigo("ROL1")).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.buscarPorCodigo("rol1"));
    }

    @Test
    void noPermiteDarDeBajaRolConUsuariosActivos() {
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setActivo(true);
        when(roles.findByCodigo("ROL1")).thenReturn(Optional.of(rol));
        when(usuarios.existsByRol_IdAndActivoTrue(1L)).thenReturn(true);
        assertThrows(OperacionNoPermitidaException.class, () -> servicio.eliminar("ROL1"));
        verify(roles, never()).save(any());
    }

    @Test
    void noPermiteCrearRolSinPermisos() {
        RolDTO dto = new RolDTO();
        dto.setNombre("VIGILANTE");
        dto.setPrefijo("VIG");
        assertThrows(OperacionNoPermitidaException.class, () -> servicio.guardar(dto));
        verify(roles, never()).save(any());
    }

    @Test
    void actualizaPermisosPorEntidad() {
        Rol rol = new Rol();
        rol.setNombre("VIGILANTE");
        rol.setActivo(true);
        RolPermisoEntidadDTO permiso = new RolPermisoEntidadDTO();
        permiso.setEntidad(Entidad.CONTRATO_VIGILANCIA);
        permiso.setPermiso(Permiso.LECTURA_DATOS_PROPIOS);
        RolDTO dto = new RolDTO();
        dto.setPermisosPorEntidad(Set.of(permiso));
        when(roles.findConBloqueoByCodigo("ROL1")).thenReturn(Optional.of(rol));
        when(roles.save(rol)).thenReturn(rol);
        RolDTO resultado = servicio.actualizar("ROL1", dto);
        assertEquals(1, resultado.getPermisosPorEntidad().size());
        assertEquals(Permiso.LECTURA_DATOS_PROPIOS, resultado.getPermisosPorEntidad().iterator().next().getPermiso());
    }
}
