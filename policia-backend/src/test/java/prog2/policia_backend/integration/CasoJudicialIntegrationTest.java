package prog2.policia_backend.integration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import prog2.policia_backend.models.Asalto;
import prog2.policia_backend.models.CasoJudicial;
import prog2.policia_backend.models.EntidadBancaria;
import prog2.policia_backend.models.Juez;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.AsaltoRepository;
import prog2.policia_backend.repositories.CasoJudicialRepository;
import prog2.policia_backend.repositories.EntidadBancariaRepository;
import prog2.policia_backend.repositories.JuezRepository;
import prog2.policia_backend.repositories.SucursalRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:casojudicialintegrationtest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CasoJudicialIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CasoJudicialRepository casoJudicialRepository;

    @Autowired
    private AsaltoRepository asaltoRepository;

    @Autowired
    private AsaltanteRepository asaltanteRepository;

    @Autowired
    private JuezRepository juezRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private EntidadBancariaRepository entidadBancariaRepository;

    private Asalto asalto;
    private Juez juez;
    private Asaltante asaltante;

    @BeforeEach
    void setUp() {
        EntidadBancaria entidad = new EntidadBancaria();
        entidad.setCodigo("EBA00001");
        entidad.setNombre("Banco Nacional");
        entidad.setDomicilioCentral("Buenos Aires");
        entidad.setActivo(true);
        entidad = entidadBancariaRepository.saveAndFlush(entidad);

        Sucursal sucursal = new Sucursal();
        sucursal.setCodigo("SUC00001");
        sucursal.setDomicilio("Calle Principal 123");
        sucursal.setCantEmpleados(10);
        sucursal.setEntidadBancaria(entidad);
        sucursal.setActivo(true);
        sucursal = sucursalRepository.saveAndFlush(sucursal);

        juez = new Juez();
        juez.setCodigo("JUE00001");
        juez.setNombre("Carlos Gomez");
        juez.setJuezDesde(LocalDate.of(2020, 1, 1));
        juez.setActivo(true);
        juez = juezRepository.saveAndFlush(juez);

        asaltante = new Asaltante();
        asaltante.setCodigo("ASS00001");
        asaltante.setNombre("Juan Perez");
        asaltante.setActivo(true);
        asaltante = asaltanteRepository.saveAndFlush(asaltante);

        List<Asaltante> participantes = new ArrayList<>();
        participantes.add(asaltante);

        asalto = new Asalto(
                LocalDate.of(2024, 5, 10),
                sucursal,
                participantes
        );

        asalto = asaltoRepository.saveAndFlush(asalto);
        asalto.asignarCodigo("AST00001");
        asalto = asaltoRepository.saveAndFlush(asalto);
    }

    private CasoJudicial crearCaso() {
        CasoJudicial caso = new CasoJudicial(
                "CJU00001",
                asalto,
                juez
        );

        return casoJudicialRepository.saveAndFlush(caso);
    }

    private String jsonCaso() {
        return """
                {
                    "asaltoCodigo": "AST00001",
                    "juezCodigo": "JUE00001"
                }
                """;
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarCasosJudicialesVacios() throws Exception {
        mockMvc.perform(get("/api/casos-judiciales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarCasosJudicialesCorrectamente() throws Exception {
        crearCaso();

        mockMvc.perform(get("/api/casos-judiciales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("CJU00001"))
                .andExpect(jsonPath("$[0].asaltoCodigo").value("AST00001"))
                .andExpect(jsonPath("$[0].juezCodigo").value("JUE00001"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarCasoPorCodigo() throws Exception {
        crearCaso();

        mockMvc.perform(get("/api/casos-judiciales/CJU00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("CJU00001"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.sentenciado").value(false));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarCasoInexistente() throws Exception {
        mockMvc.perform(get("/api/casos-judiciales/CJU99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void filtrarCasosActivos() throws Exception {
        crearCaso();

        mockMvc.perform(get("/api/casos-judiciales")
                .param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void filtrarCasosInactivos() throws Exception {
        crearCaso();

        mockMvc.perform(get("/api/casos-judiciales")
                .param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarCasosPorJuez() throws Exception {
        crearCaso();

        mockMvc.perform(get("/api/casos-judiciales/juez/JUE00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].codigo").value("CJU00001"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarCasosPorJuezSinCasos() throws Exception {
        mockMvc.perform(get("/api/casos-judiciales/juez/JUE00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarCasosDeJuezInexistente() throws Exception {
        mockMvc.perform(get("/api/casos-judiciales/juez/JUE99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarCasosDeAsaltanteInexistente() throws Exception {
        mockMvc.perform(get("/api/casos-judiciales/asaltante/ASS99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarCasosDeAsaltanteSinCasos() throws Exception {
        mockMvc.perform(get("/api/casos-judiciales/asaltante/ASS00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void crearCasoJudicialCorrectamente() throws Exception {
        mockMvc.perform(post("/api/casos-judiciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonCaso()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").exists())
                .andExpect(jsonPath("$.asaltoCodigo").value("AST00001"))
                .andExpect(jsonPath("$.juezCodigo").value("JUE00001"))
                .andExpect(jsonPath("$.asaltanteCodigos[0]").value("ASS00001"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.sentenciado").value(false));

        assertEquals(1, casoJudicialRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirCasoSinAsalto() throws Exception {
        String json = """
                {
                    "juezCodigo": "JUE00001"
                }
                """;

        mockMvc.perform(post("/api/casos-judiciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        assertEquals(0, casoJudicialRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirCasoSinJuez() throws Exception {
        String json = """
                {
                    "asaltoCodigo": "AST00001"
                }
                """;

        mockMvc.perform(post("/api/casos-judiciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        assertEquals(0, casoJudicialRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirCasoConAsaltoInexistente() throws Exception {
        String json = """
                {
                    "asaltoCodigo": "AST99999",
                    "juezCodigo": "JUE00001"
                }
                """;

        mockMvc.perform(post("/api/casos-judiciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirCasoConJuezInexistente() throws Exception {
        String json = """
                {
                    "asaltoCodigo": "AST00001",
                    "juezCodigo": "JUE99999"
                }
                """;

        mockMvc.perform(post("/api/casos-judiciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirCasoConJuezInactivo() throws Exception {
        juez.setActivo(false);
        juezRepository.saveAndFlush(juez);

        mockMvc.perform(post("/api/casos-judiciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonCaso()))
                .andExpect(status().isConflict());

        assertEquals(0, casoJudicialRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirCasoConAsaltanteInactivo() throws Exception {
        asaltante.setActivo(false);
        asaltanteRepository.saveAndFlush(asaltante);

        mockMvc.perform(post("/api/casos-judiciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonCaso()))
                .andExpect(status().isConflict());

        assertEquals(0, casoJudicialRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirCasoSinAsaltantes() throws Exception {
        List<Asaltante> participantes = new ArrayList<>();

        Asalto otroAsalto = new Asalto(
                LocalDate.of(2024, 6, 10),
                asalto.getSucursal(),
                participantes
        );

        otroAsalto = asaltoRepository.saveAndFlush(otroAsalto);
        otroAsalto.asignarCodigo("AST00002");
        asaltoRepository.saveAndFlush(otroAsalto);

        String json = """
                {
                    "asaltoCodigo": "AST00002",
                    "juezCodigo": "JUE00001"
                }
                """;

        mockMvc.perform(post("/api/casos-judiciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirDosCasosParaUnMismoAsalto() throws Exception {
        mockMvc.perform(post("/api/casos-judiciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonCaso()))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/casos-judiciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonCaso()))
                .andExpect(status().isConflict());

        assertEquals(1, casoJudicialRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void cambiarJuezCorrectamente() throws Exception {
        crearCaso();

        Juez nuevoJuez = new Juez();
        nuevoJuez.setCodigo("JUE00002");
        nuevoJuez.setNombre("Pedro Lopez");
        nuevoJuez.setJuezDesde(LocalDate.of(2021, 3, 15));
        nuevoJuez.setActivo(true);
        juezRepository.saveAndFlush(nuevoJuez);

        String json = """
                {
                    "juezCodigo": "JUE00002"
                }
                """;

        mockMvc.perform(put("/api/casos-judiciales/CJU00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.juezCodigo").value("JUE00002"));

        CasoJudicial actualizado
                = casoJudicialRepository.findByCodigo("CJU00001").get();

        assertEquals("JUE00002", actualizado.getJuez().getCodigo());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirCambioAJuezInactivo() throws Exception {
        crearCaso();

        Juez nuevoJuez = new Juez();
        nuevoJuez.setCodigo("JUE00002");
        nuevoJuez.setNombre("Pedro Lopez");
        nuevoJuez.setJuezDesde(LocalDate.of(2021, 3, 15));
        nuevoJuez.setActivo(false);
        juezRepository.saveAndFlush(nuevoJuez);

        String json = """
                {
                    "juezCodigo": "JUE00002"
                }
                """;

        mockMvc.perform(put("/api/casos-judiciales/CJU00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());

        assertEquals(
                "JUE00001",
                casoJudicialRepository.findByCodigo("CJU00001")
                        .get().getJuez().getCodigo()
        );
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void darDeBajaCasoCorrectamente() throws Exception {
        crearCaso();

        mockMvc.perform(patch("/api/casos-judiciales/CJU00001/eliminar"))
                .andExpect(status().isNoContent());

        CasoJudicial resultado
                = casoJudicialRepository.findByCodigo("CJU00001").get();

        assertFalse(resultado.isActivo());
        assertEquals(1, casoJudicialRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirBajaDeCasoInactivo() throws Exception {
        CasoJudicial caso = crearCaso();
        caso.actualizarActivo(false);
        casoJudicialRepository.saveAndFlush(caso);

        mockMvc.perform(patch("/api/casos-judiciales/CJU00001/eliminar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void reactivarCasoCorrectamente() throws Exception {
        CasoJudicial caso = crearCaso();
        caso.actualizarActivo(false);
        casoJudicialRepository.saveAndFlush(caso);

        mockMvc.perform(patch("/api/casos-judiciales/CJU00001/reactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));

        assertTrue(casoJudicialRepository
                .findByCodigo("CJU00001").get().isActivo());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirReactivarCasoActivo() throws Exception {
        crearCaso();

        mockMvc.perform(patch("/api/casos-judiciales/CJU00001/reactivar"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirActualizarCasoInactivo() throws Exception {
        CasoJudicial caso = crearCaso();
        caso.actualizarActivo(false);
        casoJudicialRepository.saveAndFlush(caso);

        String json = """
                {
                    "juezCodigo": "JUE00001"
                }
                """;

        mockMvc.perform(put("/api/casos-judiciales/CJU00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void impedirCreacionSinPermisoEscritura() throws Exception {
        mockMvc.perform(post("/api/casos-judiciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonCaso()))
                .andExpect(status().isForbidden());

        assertEquals(0, casoJudicialRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirLecturaSinPermisoLectura() throws Exception {
        mockMvc.perform(get("/api/casos-judiciales"))
                .andExpect(status().isForbidden());
    }

    @Test
    void impedirAccesoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/casos-judiciales"))
                .andExpect(status().isUnauthorized());
    }
}
