
package prog2.policia_backend.integration;

import tools.jackson.databind.ObjectMapper;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import prog2.policia_backend.DTOs.RolDTO;
import prog2.policia_backend.DTOs.RolPermisoEntidadDTO;
import prog2.policia_backend.models.Entidad;
import prog2.policia_backend.models.Permiso;
import prog2.policia_backend.models.Rol;
import prog2.policia_backend.models.RolPermisoEntidad;
import prog2.policia_backend.repositories.RolRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:rolintegrationtest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RolIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RolRepository rolRepository;

    private Rol rol;

    @BeforeEach
    void setUp() {
        rolRepository.deleteAll();

        rol = new Rol();
        rol.setCodigo("ROL00001");
        rol.setNombre("SUPERVISOR");
        rol.setPrefijo("SUP");
        rol.setDescripcion("Rol supervisor");
        rol.setActivo(true);

        RolPermisoEntidad relacion = new RolPermisoEntidad();
        relacion.setPermiso(Permiso.LECTURA);
        relacion.setEntidad(Entidad.USUARIO);
        relacion.setRol(rol);

        rol.setPermisosPorEntidad(Set.of(relacion));

        rol = rolRepository.saveAndFlush(rol);
    }

    private RolDTO crearDTO(String nombre, String prefijo) {
        RolDTO dto = new RolDTO();
        dto.setNombre(nombre);
        dto.setPrefijo(prefijo);
        dto.setDescripcion("Rol de prueba");
        dto.setPermisosPorEntidad(Set.of(new RolPermisoEntidadDTO(Permiso.LECTURA, Entidad.USUARIO)));
        return dto;
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarRolesCorrectamente() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("ROL00001"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void buscarRolPorCodigo() throws Exception {
        mockMvc.perform(get("/api/roles/ROL00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("SUPERVISOR"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void listarRolesActivos() throws Exception {
        mockMvc.perform(get("/api/roles?activo=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crearRolCorrectamente() throws Exception {
        RolDTO dto = crearDTO("OPERADOR", "OPE");

        String respuesta = mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("OPERADOR"))
                .andReturn().getResponse().getContentAsString();

        RolDTO creado = objectMapper.readValue(respuesta, RolDTO.class);

        assertNotNull(creado.getCodigo());
        assertTrue(rolRepository.findByCodigo(creado.getCodigo()).isPresent());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void actualizarRolCorrectamente() throws Exception {
        RolDTO dto = new RolDTO();
        dto.setDescripcion("Descripcion Actualizada");

        mockMvc.perform(put("/api/roles/ROL00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Descripcion Actualizada"));

        Rol actualizado = rolRepository.findByCodigo("ROL00001").get();
        assertEquals("Descripcion Actualizada", actualizado.getDescripcion());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void darDeBajaRolCorrectamente() throws Exception {
        mockMvc.perform(patch("/api/roles/ROL00001/baja"))
                .andExpect(status().isNoContent());

        Rol resultado = rolRepository.findByCodigo("ROL00001").get();
        assertFalse(resultado.getActivo());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void reactivarRolCorrectamente() throws Exception {
        rol.setActivo(false);
        rolRepository.saveAndFlush(rol);

        mockMvc.perform(patch("/api/roles/ROL00001/reactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));

        assertTrue(rolRepository.findByCodigo("ROL00001").get().getActivo());
    }

    @Test
    @WithMockUser(authorities = "LECTURA_ROL")
    void impedirCreacionSinPermisoEscritura() throws Exception {
        RolDTO dto = crearDTO("OPERADOR", "OPE");

        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        assertFalse(rolRepository.existsByNombre("OPERADOR"));
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA_ROL")
    void impedirLecturaSinPermisoLectura() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    void impedirAccesoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isUnauthorized());
    }
}
