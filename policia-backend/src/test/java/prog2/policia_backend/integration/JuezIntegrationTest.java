package prog2.policia_backend.integration;

import java.time.LocalDate;

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

import prog2.policia_backend.models.Juez;
import prog2.policia_backend.models.MotivoBajaPersona;
import prog2.policia_backend.repositories.JuezRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:juezintegrationtest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class JuezIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JuezRepository juezRepository;

    private Juez juez;

    @BeforeEach
    void setUp() {
        juez = new Juez();
        juez.setCodigo("JUE00001");
        juez.setNombre("Carlos Gomez");
        juez.setJuezDesde(LocalDate.of(2020, 1, 1));
        juez.setActivo(true);

        juez = juezRepository.saveAndFlush(juez);
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarJuecesCorrectamente() throws Exception {
        mockMvc.perform(get("/api/jueces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("JUE00001"))
                .andExpect(jsonPath("$[0].nombre").value("Carlos Gomez"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarJuezPorCodigo() throws Exception {
        mockMvc.perform(get("/api/jueces/JUE00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("JUE00001"))
                .andExpect(jsonPath("$.juezDesde").value("2020-01-01"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarJuezInexistente() throws Exception {
        mockMvc.perform(get("/api/jueces/JUE99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void filtrarJuecesActivos() throws Exception {
        mockMvc.perform(get("/api/jueces")
                .param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarJuezPorNombre() throws Exception {
        mockMvc.perform(get("/api/jueces")
                .param("nombre", "carlos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Carlos Gomez"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarCasosJudicialesSinCasos() throws Exception {
        mockMvc.perform(get("/api/jueces/JUE00001/casos-judiciales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void crearJuezCorrectamente() throws Exception {
        String json = """
                {
                    "nombre": "pedro lopez",
                    "juezDesde": "2022-03-15"
                }
                """;

        mockMvc.perform(post("/api/jueces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").exists())
                .andExpect(jsonPath("$.nombre").value("Pedro Lopez"))
                .andExpect(jsonPath("$.activo").value(true));

        assertEquals(2, juezRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirJuezSinNombre() throws Exception {
        String json = """
                {
                    "nombre": "",
                    "juezDesde": "2022-03-15"
                }
                """;

        mockMvc.perform(post("/api/jueces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        assertEquals(1, juezRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirJuezConFechaFutura() throws Exception {
        String json = """
                {
                    "nombre": "Pedro Lopez",
                    "juezDesde": "%s"
                }
                """.formatted(LocalDate.now().plusDays(10));

        mockMvc.perform(post("/api/jueces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        assertEquals(1, juezRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void actualizarJuezCorrectamente() throws Exception {
        String json = """
                {
                    "nombre": "pedro lopez",
                    "juezDesde": "2021-05-10"
                }
                """;

        mockMvc.perform(put("/api/jueces/JUE00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pedro Lopez"))
                .andExpect(jsonPath("$.juezDesde").value("2021-05-10"));

        Juez actualizado = juezRepository.findByCodigo("JUE00001").get();

        assertEquals("Pedro Lopez", actualizado.getNombre());
        assertEquals(LocalDate.of(2021, 5, 10),
                actualizado.getJuezDesde());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void darDeBajaJuezCorrectamente() throws Exception {
        String json = """
                {
                    "motivoBaja": "JUBILACION"
                }
                """;

        mockMvc.perform(patch("/api/jueces/JUE00001/baja")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent());

        Juez resultado = juezRepository.findByCodigo("JUE00001").get();

        assertFalse(resultado.isActivo());
        assertEquals(MotivoBajaPersona.JUBILACION,
                resultado.getMotivoBaja());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirBajaSinMotivo() throws Exception {
        mockMvc.perform(patch("/api/jueces/JUE00001/baja")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isConflict());

        assertTrue(juezRepository.findByCodigo("JUE00001").get().isActivo());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void reactivarJuezCorrectamente() throws Exception {
        juez.setActivo(false);
        juez.setMotivoBaja(MotivoBajaPersona.JUBILACION);
        juezRepository.saveAndFlush(juez);

        mockMvc.perform(patch("/api/jueces/JUE00001/reactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));

        Juez resultado = juezRepository.findByCodigo("JUE00001").get();

        assertTrue(resultado.isActivo());
        assertNull(resultado.getMotivoBaja());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirReactivacionPorFallecimiento() throws Exception {
        juez.setActivo(false);
        juez.setMotivoBaja(MotivoBajaPersona.FALLECIMIENTO);
        juezRepository.saveAndFlush(juez);

        mockMvc.perform(patch("/api/jueces/JUE00001/reactivar"))
                .andExpect(status().isConflict());

        assertFalse(juezRepository
                .findByCodigo("JUE00001").get().isActivo());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void impedirCreacionSinPermisoEscritura() throws Exception {
        String json = """
                {
                    "nombre": "Pedro Lopez",
                    "juezDesde": "2022-03-15"
                }
                """;

        mockMvc.perform(post("/api/jueces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());

        assertEquals(1, juezRepository.count());
    }

    @Test
    void impedirAccesoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/jueces"))
                .andExpect(status().isUnauthorized());
    }
}
