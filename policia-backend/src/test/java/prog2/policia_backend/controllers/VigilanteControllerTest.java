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
import prog2.policia_backend.services.VigilanteService;
import prog2.policia_backend.services.ContratoVigilanciaService;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VigilanteController.class)
@ContextConfiguration(classes = {VigilanteController.class, SecurityConfig.class})
class VigilanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VigilanteService vigilanteService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ContratoVigilanciaService contratoVigilanciaService;

    private UsuarioDetails crearUsuario(String codigo, Set<Permiso> permisos) {
        return new UsuarioDetails(1L, codigo, "password", permisos, true);
    }

    @Test
    void lecturaPuedeConsultarOtroVigilante() throws Exception {
        UsuarioDetails usuario = crearUsuario("ADM00001", Set.of(Permiso.LECTURA));

        mockMvc.perform(get("/api/vigilantes/VIG00002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());

        verify(vigilanteService).buscarPorCodigo("VIG00002");
    }

    @Test
    void lecturaDatosPropiosPuedeConsultarSuCodigo() throws Exception {
        UsuarioDetails usuario = crearUsuario("VIG00001", Set.of(Permiso.LECTURA_DATOS_PROPIOS));

        mockMvc.perform(get("/api/vigilantes/VIG00001").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());

        verify(vigilanteService).buscarPorCodigo("VIG00001");
    }

    @Test
    void lecturaDatosPropiosNoPuedeConsultarOtroVigilante() throws Exception {
        UsuarioDetails usuario = crearUsuario("VIG00001", Set.of(Permiso.LECTURA_DATOS_PROPIOS));

        mockMvc.perform(get("/api/vigilantes/VIG00002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verify(vigilanteService, never()).buscarPorCodigo("VIG00002");
    }

    @Test
    void ambosPermisosPuedenConsultarOtroVigilante() throws Exception {
        UsuarioDetails usuario = crearUsuario("VIG00001", Set.of(Permiso.LECTURA, Permiso.LECTURA_DATOS_PROPIOS));

        mockMvc.perform(get("/api/vigilantes/VIG00002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());

        verify(vigilanteService).buscarPorCodigo("VIG00002");
    }

    @Test
    void sinPermisosNoPuedeConsultarVigilantes() throws Exception {
        UsuarioDetails usuario = crearUsuario("VIG00001", Set.of());

        mockMvc.perform(get("/api/vigilantes/VIG00002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(vigilanteService);
    }
}
