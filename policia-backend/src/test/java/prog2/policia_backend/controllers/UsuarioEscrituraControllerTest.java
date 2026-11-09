package prog2.policia_backend.controllers;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import prog2.policia_backend.DTOs.UsuarioDTO;
import prog2.policia_backend.configs.SecurityConfig;
import prog2.policia_backend.models.Entidad;
import prog2.policia_backend.models.Permiso;
import prog2.policia_backend.models.RolPermisoEntidad;
import prog2.policia_backend.security.UsuarioDetails;
import prog2.policia_backend.services.UsuarioService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.context.annotation.Import(prog2.policia_backend.ConfiguracionMockMvc.class)
@WebMvcTest(UsuarioController.class)
@ContextConfiguration(classes = {UsuarioController.class, SecurityConfig.class, prog2.policia_backend.security.AutorizadorPermisos.class})
class UsuarioEscrituraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UsuarioDetails crearUsuario(String codigo, String nombreRol, Permiso... permisos) {
        Set<RolPermisoEntidad> relaciones = new HashSet<>();

        for (Permiso permiso : permisos) {
            RolPermisoEntidad relacion = new RolPermisoEntidad();
            relacion.setPermiso(permiso);
            relacion.setEntidad(Entidad.USUARIO);
            relaciones.add(relacion);
        }

        return new UsuarioDetails(1L, codigo, "password", nombreRol, relaciones, true);
    }

    @Test
    void lecturaNoPuedeRegistrarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001", "OPERADOR", Permiso.LECTURA);

        mockMvc.perform(post("/api/usuarios").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void lecturaNoPuedeActualizarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001", "OPERADOR", Permiso.LECTURA);

        mockMvc.perform(put("/api/usuarios/USR00002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void sinPermisosNoPuedeRegistrarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001", "OPERADOR");

        mockMvc.perform(post("/api/usuarios").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void sinPermisosNoPuedeActualizarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001", "OPERADOR");

        mockMvc.perform(put("/api/usuarios/USR00002").with(SecurityMockMvcRequestPostProcessors.user(usuario)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void administradorPuedeRegistrarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001", "OPERADOR", Permiso.ESCRITURA);

        String json = """
        {
            "nombre": "Juan Perez",
            "password": "clave123",
            "rol": "ADMINISTRADOR",
            "fechaNacimiento": "1995-05-20"
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
    void administradorPuedeActualizarUsuarios() throws Exception {
        UsuarioDetails usuario = crearUsuario("USR00001", "OPERADOR", Permiso.ESCRITURA);

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
