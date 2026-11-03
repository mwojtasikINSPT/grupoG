package prog2.policia_backend.controllers;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import prog2.policia_backend.configs.SecurityConfig;
import prog2.policia_backend.models.Entidad;
import prog2.policia_backend.models.Permiso;
import prog2.policia_backend.models.RolPermisoEntidad;
import prog2.policia_backend.security.UsuarioDetails;
import prog2.policia_backend.services.UsuarioService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.context.annotation.Import(prog2.policia_backend.ConfiguracionMockMvc.class)
@WebMvcTest(UsuarioController.class)
@ContextConfiguration(classes = {UsuarioController.class, SecurityConfig.class, prog2.policia_backend.security.AutorizadorPermisos.class})
class UsuarioListadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UsuarioDetails crearUsuario(String codigo, Permiso... permisos) {
        Set<RolPermisoEntidad> relaciones = new HashSet<>();

        for (Permiso permiso : permisos) {
            RolPermisoEntidad relacion = new RolPermisoEntidad();
            relacion.setPermiso(permiso);
            relacion.setEntidad(Entidad.USUARIO);
            relaciones.add(relacion);
        }

        return new UsuarioDetails(1L, codigo, "password", "OPERADOR", relaciones, true);
    }

    @Test
    void lecturaPuedeListarTodosLosUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("ADM00001", Permiso.LECTURA);

        mockMvc.perform(get("/api/usuarios").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());

        verify(usuarioService).listar(null);
        verify(usuarioService, never()).listarPropio("ADM00001", null, null);
    }

    @Test
    void lecturaDatosPropiosSoloPuedeListarSusDatos() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001", Permiso.LECTURA_DATOS_PROPIOS);

        mockMvc.perform(get("/api/usuarios").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());

        verify(usuarioService).listarPropio("USR00001", null, null);
        verify(usuarioService, never()).listar(null);
    }

    @Test
    void ambosPermisosPuedenListarTodosLosUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001", Permiso.LECTURA, Permiso.LECTURA_DATOS_PROPIOS);

        mockMvc.perform(get("/api/usuarios").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());

        verify(usuarioService).listar(null);
        verify(usuarioService, never()).listarPropio("USR00001", null, null);
    }

    @Test
    void sinPermisosNoPuedeListarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001");

        mockMvc.perform(get("/api/usuarios").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }
}
