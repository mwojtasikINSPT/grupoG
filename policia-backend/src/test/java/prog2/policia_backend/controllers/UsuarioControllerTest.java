package prog2.policia_backend.controllers;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;

import prog2.policia_backend.models.Permiso;
import prog2.policia_backend.security.UsuarioDetails;
import prog2.policia_backend.services.UsuarioService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import prog2.policia_backend.configs.SecurityConfig;

@WebMvcTest(UsuarioController.class)
@ContextConfiguration(classes = {UsuarioController.class, SecurityConfig.class})
class UsuarioControllerTest {

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
    void lecturaPuedeConsultarOtroUsuario() throws Exception {
        UsuarioDetails usuario = crearUsuario("ADM001", Set.of(Permiso.LECTURA));

        mockMvc.perform(get("/api/usuarios/USR002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());

        verify(usuarioService).buscarPorCodigo("USR002");
    }

    @Test
    void lecturaDatosPropiosPuedeConsultarSuCodigo() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR001", Set.of(Permiso.LECTURA_DATOS_PROPIOS));

        mockMvc.perform(get("/api/usuarios/USR001").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());

        verify(usuarioService).buscarPorCodigo("USR001");
    }

    @Test
    void lecturaDatosPropiosNoPuedeConsultarOtroCodigo() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR001", Set.of(Permiso.LECTURA_DATOS_PROPIOS));

        mockMvc.perform(get("/api/usuarios/USR002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).buscarPorCodigo("USR002");
    }

    @Test
    void ambosPermisosPuedenConsultarOtroUsuario() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR001", Set.of(Permiso.LECTURA, Permiso.LECTURA_DATOS_PROPIOS));

        mockMvc.perform(get("/api/usuarios/USR002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());

        verify(usuarioService).buscarPorCodigo("USR002");
    }

    @Test
    void sinPermisosNoPuedeConsultarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR001", Set.of());

        mockMvc.perform(get("/api/usuarios/USR002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).buscarPorCodigo("USR002");
    }
}
