package prog2.policia_backend.integration;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.models.Banda;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.BandaRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:bandaintegrationtest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BandaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BandaRepository bandaRepository;

    @Autowired
    private AsaltanteRepository asaltanteRepository;

    @Autowired
    private EntityManager entityManager;

    private Banda banda;

    @BeforeEach
    void setUp() {
        banda = new Banda();
        banda.setCodigo("BAN00001");
        banda.setActivo(true);
        banda = bandaRepository.saveAndFlush(banda);
    }

    private Asaltante crearAsaltante(String codigo, boolean activo) {
        Asaltante asaltante = new Asaltante();
        asaltante.setCodigo(codigo);
        asaltante.setNombre("Juan Perez");
        asaltante.setBanda(banda);
        asaltante.setActivo(activo);

        return asaltanteRepository.saveAndFlush(asaltante);
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarBandasCorrectamente() throws Exception {
        mockMvc.perform(get("/api/bandas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("BAN00001"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarBandaPorCodigo() throws Exception {
        mockMvc.perform(get("/api/bandas/BAN00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("BAN00001"))
                .andExpect(jsonPath("$.cantMiembros").value(0));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarBandaInexistente() throws Exception {
        mockMvc.perform(get("/api/bandas/BAN99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void filtrarBandasActivas() throws Exception {
        mockMvc.perform(get("/api/bandas")
                .param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("BAN00001"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void filtrarBandasInactivas() throws Exception {
        mockMvc.perform(get("/api/bandas")
                .param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void crearBandaCorrectamente() throws Exception {
        mockMvc.perform(post("/api/bandas"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").exists())
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.cantMiembros").value(0));

        assertEquals(2, bandaRepository.count());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void bandaSinMiembrosTieneCantidadCero() throws Exception {
        mockMvc.perform(get("/api/bandas/BAN00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantMiembros").value(0));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void contarMiembrosActivosCorrectamente() throws Exception {
        crearAsaltante("ASS00001", true);
        crearAsaltante("ASS00002", true);

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/bandas/BAN00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantMiembros").value(2));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void noContarMiembrosInactivos() throws Exception {
        crearAsaltante("ASS00001", true);
        crearAsaltante("ASS00002", false);

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/bandas/BAN00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantMiembros").value(1));
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void darDeBajaBandaCorrectamente() throws Exception {
        mockMvc.perform(patch("/api/bandas/BAN00001/baja"))
                .andExpect(status().isNoContent());

        Banda resultado = bandaRepository.findByCodigo("BAN00001").get();

        assertFalse(resultado.isActivo());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirBajaConMiembrosActivos() throws Exception {
        crearAsaltante("ASS00001", true);

        mockMvc.perform(patch("/api/bandas/BAN00001/baja"))
                .andExpect(status().isConflict());

        assertTrue(bandaRepository.findByCodigo("BAN00001").get().isActivo());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void permitirBajaConMiembrosInactivos() throws Exception {
        crearAsaltante("ASS00001", false);

        mockMvc.perform(patch("/api/bandas/BAN00001/baja"))
                .andExpect(status().isNoContent());

        assertFalse(bandaRepository.findByCodigo("BAN00001").get().isActivo());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirBajaDeBandaInactiva() throws Exception {
        banda.setActivo(false);
        bandaRepository.saveAndFlush(banda);

        mockMvc.perform(patch("/api/bandas/BAN00001/baja"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void reactivarBandaCorrectamente() throws Exception {
        banda.setActivo(false);
        bandaRepository.saveAndFlush(banda);

        mockMvc.perform(patch("/api/bandas/BAN00001/reactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));

        assertTrue(bandaRepository.findByCodigo("BAN00001").get().isActivo());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirReactivarBandaActiva() throws Exception {
        mockMvc.perform(patch("/api/bandas/BAN00001/reactivar"))
                .andExpect(status().isConflict());

        assertTrue(bandaRepository.findByCodigo("BAN00001").get().isActivo());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void impedirCreacionSinPermisoEscritura() throws Exception {
        mockMvc.perform(post("/api/bandas"))
                .andExpect(status().isForbidden());

        assertEquals(1, bandaRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirLecturaSinPermisoLectura() throws Exception {
        mockMvc.perform(get("/api/bandas"))
                .andExpect(status().isForbidden());
    }

    @Test
    void impedirAccesoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/bandas"))
                .andExpect(status().isUnauthorized());
    }
}
