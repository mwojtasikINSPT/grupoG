package prog2.policia_backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import prog2.policia_backend.configs.SecurityConfig;
import prog2.policia_backend.models.Permiso;
import prog2.policia_backend.security.UsuarioDetails;
import prog2.policia_backend.services.UsuarioService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import prog2.policia_backend.DTOs.UsuarioDTO;

@WebMvcTest(UsuarioController.class)
@ContextConfiguration(classes = {UsuarioController.class, SecurityConfig.class})
class UsuarioEscrituraControllerTest {

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
    void lecturaNoPuedeRegistrarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001", Set.of(Permiso.LECTURA));

        mockMvc.perform(post("/api/usuarios").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void lecturaNoPuedeActualizarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001", Set.of(Permiso.LECTURA));

        mockMvc.perform(put("/api/usuarios/USR00002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void sinPermisosNoPuedeRegistrarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001", Set.of());

        mockMvc.perform(post("/api/usuarios").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void sinPermisosNoPuedeActualizarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001", Set.of());

        mockMvc.perform(put("/api/usuarios/USR00002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void escrituraPuedeRegistrarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("ADM00001", Set.of(Permiso.ESCRITURA));

        String json = """
            {
                "nombre": "Juan Perez",
                "password": "clave123",
                "rol": "ADMINISTRADOR"
            }
            """;

        mockMvc.perform(post("/api/usuarios")
                .with(SecurityMockMvcRequestPostProcessors.user(usuario))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        verify(usuarioService).guardar(any(UsuarioDTO.class));
    }

    @Test
    void escrituraPuedeActualizarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("ADM00001", Set.of(Permiso.ESCRITURA));

        String json = """
            {
                "nombre": "Juan Actualizado"
            }
            """;

        mockMvc.perform(put("/api/usuarios/USR00002")
                .with(SecurityMockMvcRequestPostProcessors.user(usuario))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        verify(usuarioService).actualizar(eq("USR00002"), any(UsuarioDTO.class));
    }

}
