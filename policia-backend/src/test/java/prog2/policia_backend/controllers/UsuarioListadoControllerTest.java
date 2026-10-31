package prog2.policia_backend.controllers;

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
import prog2.policia_backend.models.Permiso;
import prog2.policia_backend.security.UsuarioDetails;
import prog2.policia_backend.services.UsuarioService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@ContextConfiguration(classes = {UsuarioController.class, SecurityConfig.class})
class UsuarioListadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UsuarioDetails crearUsuario(String codigo, Set<Permiso> permisos) {
        return new UsuarioDetails(1L, codigo, "password", permisos, true);
    }

    @Test
    void lecturaPuedeListarTodosLosUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("ADM00001", Set.of(Permiso.LECTURA));

        mockMvc.perform(get("/api/usuarios").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());

        verify(usuarioService).listar(null);
        verify(usuarioService, never()).listarPropio("ADM00001", null, null);
    }

    @Test
    void lecturaDatosPropiosSoloPuedeListarSusDatos() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001", Set.of(Permiso.LECTURA_DATOS_PROPIOS));

        mockMvc.perform(get("/api/usuarios").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());

        verify(usuarioService).listarPropio("USR00001", null, null);
        verify(usuarioService, never()).listar(null);
    }

    @Test
    void ambosPermisosPuedenListarTodosLosUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001", Set.of(Permiso.LECTURA, Permiso.LECTURA_DATOS_PROPIOS));

        mockMvc.perform(get("/api/usuarios").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());

        verify(usuarioService).listar(null);
        verify(usuarioService, never()).listarPropio("USR00001", null, null);
    }

    @Test
    void sinPermisosNoPuedeListarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001", Set.of());

        mockMvc.perform(get("/api/usuarios").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }
}
