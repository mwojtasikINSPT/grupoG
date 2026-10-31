
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
import prog2.policia_backend.services.ContratoVigilanciaService;
import prog2.policia_backend.services.VigilanteService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VigilanteController.class)
@ContextConfiguration(classes = {VigilanteController.class, SecurityConfig.class})
class VigilanteContratosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VigilanteService vigilanteService;

    @MockitoBean
    private ContratoVigilanciaService contratoVigilanciaService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UsuarioDetails crearUsuario(String codigo, Set<Permiso> permisos) {
        return new UsuarioDetails(1L, codigo, "password", permisos, true);
    }

    @Test
    void vigilantePuedeConsultarSusContratos() throws Exception {
        UsuarioDetails usuario = crearUsuario("VIG00001", Set.of(Permiso.LECTURA_DATOS_PROPIOS));

        mockMvc.perform(get("/api/vigilantes/VIG00001/contratos").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());
    }

    @Test
    void vigilanteNoPuedeConsultarContratosAjenos() throws Exception {
        UsuarioDetails usuario = crearUsuario("VIG00001", Set.of(Permiso.LECTURA_DATOS_PROPIOS));

        mockMvc.perform(get("/api/vigilantes/VIG00002/contratos").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());
    }

    @Test
    void lecturaPuedeConsultarContratosDeOtroVigilante() throws Exception {
        UsuarioDetails usuario = crearUsuario("ADM00001", Set.of(Permiso.LECTURA));

        mockMvc.perform(get("/api/vigilantes/VIG00002/contratos").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());
    }

    @Test
    void sinPermisosNoPuedeConsultarContratos() throws Exception {
        UsuarioDetails usuario = crearUsuario("VIG00001", Set.of());

        mockMvc.perform(get("/api/vigilantes/VIG00001/contratos").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());
    }
}
