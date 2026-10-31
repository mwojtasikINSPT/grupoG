package prog2.policia_backend.integration;

import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import prog2.policia_backend.models.MotivoBajaPersona;
import prog2.policia_backend.models.Permiso;
import prog2.policia_backend.models.Rol;
import prog2.policia_backend.models.Vigilante;
import prog2.policia_backend.repositories.RolRepository;
import prog2.policia_backend.repositories.VigilanteRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:vigilanteintegrationtest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class VigilanteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VigilanteRepository vigilanteRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Vigilante vigilante;

    @BeforeEach
    void setUp() {
        Rol rol = new Rol();
        rol.setCodigo("ROL00001");
        rol.setNombre("VIGILANTE");
        rol.setPrefijo("VIG");
        rol.setDescripcion("Rol de vigilancia");
        rol.setPermisos(EnumSet.of(Permiso.LECTURA_DATOS_PROPIOS));
        rol.setActivo(true);

        rol = rolRepository.saveAndFlush(rol);

        vigilante = new Vigilante();
        vigilante.setCodigo("VIG00001");
        vigilante.setNombre("Juan Perez");
        vigilante.setPassword(passwordEncoder.encode("clave123"));
        vigilante.setEdad(30);
        vigilante.setRol(rol);
        vigilante.setActivo(true);

        vigilante = vigilanteRepository.saveAndFlush(vigilante);
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarVigilantesCorrectamente() throws Exception {
        mockMvc.perform(get("/api/vigilantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("VIG00001"))
                .andExpect(jsonPath("$[0].nombre").value("Juan Perez"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarVigilantePorCodigo() throws Exception {
        mockMvc.perform(get("/api/vigilantes/VIG00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("VIG00001"))
                .andExpect(jsonPath("$.edad").value(30));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarVigilantePorNombre() throws Exception {
        mockMvc.perform(get("/api/vigilantes")
                .param("nombre", "juan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Juan Perez"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void filtrarVigilantesActivos() throws Exception {
        mockMvc.perform(get("/api/vigilantes")
                .param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void crearVigilanteCorrectamente() throws Exception {
        String json = """
                {
                    "nombre": "pedro gomez",
                    "password": "clave456",
                    "edad": 25
                }
                """;

        mockMvc.perform(post("/api/vigilantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Pedro Gomez"))
                .andExpect(jsonPath("$.edad").value(25))
                .andExpect(jsonPath("$.codigo").exists());

        assertEquals(2, vigilanteRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void actualizarVigilanteCorrectamente() throws Exception {
        String json = """
                {
                    "nombre": "pedro lopez",
                    "edad": 35
                }
                """;

        mockMvc.perform(put("/api/vigilantes/VIG00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pedro Lopez"))
                .andExpect(jsonPath("$.edad").value(35));

        Vigilante actualizado
                = vigilanteRepository.findByCodigo("VIG00001").get();

        assertEquals("Pedro Lopez", actualizado.getNombre());
        assertEquals(35, actualizado.getEdad());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void rechazarVigilanteMenorDeEdad() throws Exception {
        String json = """
                {
                    "nombre": "Pedro Gomez",
                    "password": "clave456",
                    "edad": 17
                }
                """;

        mockMvc.perform(post("/api/vigilantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        assertEquals(1, vigilanteRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void darDeBajaVigilanteCorrectamente() throws Exception {
        String json = """
                {
                    "motivoBaja": "RENUNCIA"
                }
                """;

        mockMvc.perform(patch("/api/vigilantes/VIG00001/baja")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent());

        Vigilante resultado
                = vigilanteRepository.findByCodigo("VIG00001").get();

        assertFalse(resultado.isActivo());
        assertEquals(MotivoBajaPersona.RENUNCIA,
                resultado.getMotivoBaja());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void reactivarVigilanteCorrectamente() throws Exception {
        vigilante.setActivo(false);
        vigilante.setMotivoBaja(MotivoBajaPersona.RENUNCIA);
        vigilanteRepository.saveAndFlush(vigilante);

        mockMvc.perform(patch("/api/vigilantes/VIG00001/reactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));

        Vigilante resultado
                = vigilanteRepository.findByCodigo("VIG00001").get();

        assertTrue(resultado.isActivo());
        assertNull(resultado.getMotivoBaja());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void impedirCreacionSinPermisoEscritura() throws Exception {
        String json = """
                {
                    "nombre": "Pedro Gomez",
                    "password": "clave456",
                    "edad": 25
                }
                """;

        mockMvc.perform(post("/api/vigilantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());

        assertEquals(1, vigilanteRepository.count());
    }
}
