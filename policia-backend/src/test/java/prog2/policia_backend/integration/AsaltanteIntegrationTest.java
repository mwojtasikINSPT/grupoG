
package prog2.policia_backend.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.models.Banda;
import prog2.policia_backend.models.MotivoBajaPersona;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.BandaRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:asaltanteintegrationtest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AsaltanteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AsaltanteRepository asaltanteRepository;

    @Autowired
    private BandaRepository bandaRepository;

    private Asaltante asaltante;
    private Banda banda;

    @BeforeEach
    void setUp() {
        banda = new Banda();
        banda.setCodigo("BAN00001");
        banda.setActivo(true);
        banda = bandaRepository.saveAndFlush(banda);

        asaltante = new Asaltante();
        asaltante.setCodigo("ASS00001");
        asaltante.setNombre("Juan Perez");
        asaltante.setBanda(banda);
        asaltante.setActivo(true);
        asaltante = asaltanteRepository.saveAndFlush(asaltante);
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarAsaltantesCorrectamente() throws Exception {
        mockMvc.perform(get("/api/asaltantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("ASS00001"))
                .andExpect(jsonPath("$[0].nombre").value("Juan Perez"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarAsaltantePorCodigo() throws Exception {
        mockMvc.perform(get("/api/asaltantes/ASS00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("ASS00001"))
                .andExpect(jsonPath("$.bandaCodigo").value("BAN00001"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarAsaltanteInexistente() throws Exception {
        mockMvc.perform(get("/api/asaltantes/ASS99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void filtrarAsaltantesActivos() throws Exception {
        mockMvc.perform(get("/api/asaltantes")
                .param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void filtrarAsaltantesInactivos() throws Exception {
        mockMvc.perform(get("/api/asaltantes")
                .param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarAsaltantePorNombre() throws Exception {
        mockMvc.perform(get("/api/asaltantes")
                .param("nombre", "juan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Juan Perez"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarAsaltantePorNombreInexistente() throws Exception {
        mockMvc.perform(get("/api/asaltantes")
                .param("nombre", "Carlos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarAsaltosSinAsaltos() throws Exception {
        mockMvc.perform(get("/api/asaltantes/ASS00001/asaltos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarCasosJudicialesSinCasos() throws Exception {
        mockMvc.perform(get("/api/asaltantes/ASS00001/casos-judiciales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void crearAsaltanteCorrectamente() throws Exception {
        String json = """
                {
                    "nombre": "pedro gomez",
                    "bandaCodigo": "BAN00001"
                }
                """;

        mockMvc.perform(post("/api/asaltantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Pedro Gomez"))
                .andExpect(jsonPath("$.bandaCodigo").value("BAN00001"))
                .andExpect(jsonPath("$.codigo").exists())
                .andExpect(jsonPath("$.activo").value(true));

        assertEquals(2, asaltanteRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void crearAsaltanteSinBanda() throws Exception {
        String json = """
                {
                    "nombre": "Pedro Gomez"
                }
                """;

        mockMvc.perform(post("/api/asaltantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Pedro Gomez"))
                .andExpect(jsonPath("$.bandaCodigo").doesNotExist());

        assertEquals(2, asaltanteRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirAsaltanteSinNombre() throws Exception {
        String json = """
                {
                    "nombre": "",
                    "bandaCodigo": "BAN00001"
                }
                """;

        mockMvc.perform(post("/api/asaltantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        assertEquals(1, asaltanteRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirAsaltanteConBandaInactiva() throws Exception {
        banda.setActivo(false);
        bandaRepository.saveAndFlush(banda);

        String json = """
                {
                    "nombre": "Pedro Gomez",
                    "bandaCodigo": "BAN00001"
                }
                """;

        mockMvc.perform(post("/api/asaltantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());

        assertEquals(1, asaltanteRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void actualizarAsaltanteCorrectamente() throws Exception {
        String json = """
                {
                    "nombre": "pedro lopez"
                }
                """;

        mockMvc.perform(put("/api/asaltantes/ASS00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pedro Lopez"));

        Asaltante actualizado =
                asaltanteRepository.findByCodigo("ASS00001").get();

        assertEquals("Pedro Lopez", actualizado.getNombre());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void quitarAsaltanteDeBanda() throws Exception {
        String json = """
                {
                    "quitarDeBanda": true
                }
                """;

        mockMvc.perform(put("/api/asaltantes/ASS00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bandaCodigo").doesNotExist());

        Asaltante actualizado =
                asaltanteRepository.findByCodigo("ASS00001").get();

        assertNull(actualizado.getBanda());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void darDeBajaAsaltanteCorrectamente() throws Exception {
        String json = """
                {
                    "motivoBaja": "FALLECIMIENTO"
                }
                """;

        mockMvc.perform(patch("/api/asaltantes/ASS00001/baja")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent());

        Asaltante resultado =
                asaltanteRepository.findByCodigo("ASS00001").get();

        assertFalse(resultado.isActivo());
        assertEquals(MotivoBajaPersona.FALLECIMIENTO,
                resultado.getMotivoBaja());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirBajaSinMotivo() throws Exception {
        mockMvc.perform(patch("/api/asaltantes/ASS00001/baja")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isConflict());

        assertTrue(asaltanteRepository
                .findByCodigo("ASS00001").get().isActivo());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirBajaConMotivoInvalido() throws Exception {
        String json = """
                {
                    "motivoBaja": "RENUNCIA"
                }
                """;

        mockMvc.perform(patch("/api/asaltantes/ASS00001/baja")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());

        assertTrue(asaltanteRepository
                .findByCodigo("ASS00001").get().isActivo());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirModificarAsaltanteInactivo() throws Exception {
        asaltante.setActivo(false);
        asaltante.setMotivoBaja(MotivoBajaPersona.FALLECIMIENTO);
        asaltanteRepository.saveAndFlush(asaltante);

        String json = """
                {
                    "nombre": "Pedro Lopez"
                }
                """;

        mockMvc.perform(put("/api/asaltantes/ASS00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void impedirCreacionSinPermisoEscritura() throws Exception {
        String json = """
                {
                    "nombre": "Pedro Gomez"
                }
                """;

        mockMvc.perform(post("/api/asaltantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());

        assertEquals(1, asaltanteRepository.count());
    }

    @Test
    void impedirAccesoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/asaltantes"))
                .andExpect(status().isUnauthorized());
    }
}
