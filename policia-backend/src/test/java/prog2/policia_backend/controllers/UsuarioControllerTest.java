
package prog2.policia_backend.controllers;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;

import prog2.policia_backend.models.Entidad;
import prog2.policia_backend.models.Permiso;
import prog2.policia_backend.models.RolPermisoEntidad;
import prog2.policia_backend.models.Rol;
import prog2.policia_backend.security.UsuarioDetails;
import prog2.policia_backend.services.UsuarioService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import prog2.policia_backend.configs.SecurityConfig;

@org.springframework.context.annotation.Import(prog2.policia_backend.ConfiguracionMockMvc.class)
@WebMvcTest(UsuarioController.class)
@ContextConfiguration(classes = {UsuarioController.class, SecurityConfig.class, prog2.policia_backend.security.AutorizadorPermisos.class, prog2.policia_backend.security.SeguridadAdministrativa.class, prog2.policia_backend.security.ManejadorAccesoDenegado.class, prog2.policia_backend.exceptions.GlobalExceptionHandler.class})
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UsuarioDetails crearUsuario(String codigo, Permiso... permisos) {
        Set<RolPermisoEntidad> relaciones = new HashSet<>();

        for (Permiso permiso : permisos) {
            RolPermisoEntidad relacion = new RolPermisoEntidad(new Rol(), permiso, Entidad.USUARIO);
            relaciones.add(relacion);
        }

        return new UsuarioDetails(1L, codigo, "password", "OPERADOR", relaciones, true);
    }

    @Test
    void lecturaPuedeConsultarOtroUsuario() throws Exception {
        UsuarioDetails usuario = crearUsuario("ADM001", Permiso.LECTURA);

        mockMvc.perform(get("/api/usuarios/USR002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());

        verify(usuarioService).buscarPorCodigoAutorizado(eq("USR002"), any(org.springframework.security.core.Authentication.class));
    }

    @Test
    void lecturaDatosPropiosPuedeConsultarSuCodigo() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR001", Permiso.LECTURA_DATOS_PROPIOS);

        mockMvc.perform(get("/api/usuarios/USR001").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());

        verify(usuarioService).buscarPorCodigoAutorizado(eq("USR001"), any(org.springframework.security.core.Authentication.class));
    }

    @Test
    void lecturaDatosPropiosNoPuedeConsultarOtroCodigo() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR001", Permiso.LECTURA_DATOS_PROPIOS);
        when(usuarioService.buscarPorCodigoAutorizado(eq("USR002"), any(org.springframework.security.core.Authentication.class)))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("No tiene permiso para consultar este usuario"));

        mockMvc.perform(get("/api/usuarios/USR002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verify(usuarioService).buscarPorCodigoAutorizado(eq("USR002"), any(org.springframework.security.core.Authentication.class));
    }

    @Test
    void ambosPermisosPuedenConsultarOtroUsuario() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR001", Permiso.LECTURA, Permiso.LECTURA_DATOS_PROPIOS);

        mockMvc.perform(get("/api/usuarios/USR002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isOk());

        verify(usuarioService).buscarPorCodigoAutorizado(eq("USR002"), any(org.springframework.security.core.Authentication.class));
    }

    @Test
    void sinPermisosNoPuedeConsultarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR001");

        mockMvc.perform(get("/api/usuarios/USR002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).buscarPorCodigo("USR002");
    }
}
