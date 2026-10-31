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

import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.models.Asalto;
import prog2.policia_backend.models.EntidadBancaria;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.AsaltoRepository;
import prog2.policia_backend.repositories.EntidadBancariaRepository;
import prog2.policia_backend.repositories.SucursalRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:asaltointegrationtest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AsaltoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AsaltoRepository asaltoRepository;

    @Autowired
    private AsaltanteRepository asaltanteRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private EntidadBancariaRepository entidadBancariaRepository;

    private Sucursal sucursal;
    private Asaltante asaltante;

    @BeforeEach
    void setUp() {
        EntidadBancaria entidad = new EntidadBancaria();
        entidad.setCodigo("EBA00001");
        entidad.setNombre("Banco Nacional");
        entidad.setDomicilioCentral("Buenos Aires");
        entidad.setActivo(true);
        entidad = entidadBancariaRepository.saveAndFlush(entidad);

        sucursal = new Sucursal();
        sucursal.setCodigo("SUC00001");
        sucursal.setDomicilio("Calle Principal 123");
        sucursal.setCantEmpleados(10);
        sucursal.setEntidadBancaria(entidad);
        sucursal.setActivo(true);
        sucursal = sucursalRepository.saveAndFlush(sucursal);

        asaltante = new Asaltante();
        asaltante.setCodigo("ASS00001");
        asaltante.setNombre("Juan Perez");
        asaltante.setActivo(true);
        asaltante = asaltanteRepository.saveAndFlush(asaltante);
    }

    private Asalto crearAsalto() {
        java.util.List<Asaltante> participantes = new java.util.ArrayList<>();
        participantes.add(asaltante);

        Asalto asalto = new Asalto(
                LocalDate.of(2024, 5, 10),
                sucursal,
                participantes
        );

        asalto = asaltoRepository.saveAndFlush(asalto);
        asalto.asignarCodigo("AST00001");

        return asaltoRepository.saveAndFlush(asalto);
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarAsaltosCorrectamente() throws Exception {
        crearAsalto();

        mockMvc.perform(get("/api/asaltos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("AST00001"))
                .andExpect(jsonPath("$[0].sucursalCodigo").value("SUC00001"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarAsaltosVacios() throws Exception {
        mockMvc.perform(get("/api/asaltos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarAsaltoPorCodigo() throws Exception {
        crearAsalto();

        mockMvc.perform(get("/api/asaltos/AST00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("AST00001"))
                .andExpect(jsonPath("$.fecha").value("2024-05-10"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarAsaltoInexistente() throws Exception {
        mockMvc.perform(get("/api/asaltos/AST99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarAsaltosPorAsaltante() throws Exception {
        crearAsalto();

        mockMvc.perform(get("/api/asaltos/asaltante/ASS00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].codigo").value("AST00001"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarAsaltosDeAsaltanteInexistente() throws Exception {
        mockMvc.perform(get("/api/asaltos/asaltante/ASS99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarAsaltosPorSucursal() throws Exception {
        crearAsalto();

        mockMvc.perform(get("/api/asaltos/sucursal/SUC00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].codigo").value("AST00001"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarAsaltosDeSucursalInexistente() throws Exception {
        mockMvc.perform(get("/api/asaltos/sucursal/SUC99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void crearAsaltoCorrectamente() throws Exception {
        String json = """
                {
                    "fecha": "2024-05-10",
                    "sucursalCodigo": "SUC00001",
                    "asaltantesCodigos": ["ASS00001"]
                }
                """;

        mockMvc.perform(post("/api/asaltos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").exists())
                .andExpect(jsonPath("$.fecha").value("2024-05-10"))
                .andExpect(jsonPath("$.sucursalCodigo").value("SUC00001"))
                .andExpect(jsonPath("$.asaltantesCodigos[0]").value("ASS00001"));

        assertEquals(1, asaltoRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void crearAsaltoSinAsaltantes() throws Exception {
        String json = """
                {
                    "fecha": "2024-05-10",
                    "sucursalCodigo": "SUC00001",
                    "asaltantesCodigos": []
                }
                """;

        mockMvc.perform(post("/api/asaltos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.asaltantesCodigos.length()").value(0));

        assertEquals(1, asaltoRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirAsaltoSinFecha() throws Exception {
        String json = """
                {
                    "sucursalCodigo": "SUC00001",
                    "asaltantesCodigos": ["ASS00001"]
                }
                """;

        mockMvc.perform(post("/api/asaltos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        assertEquals(0, asaltoRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirAsaltoConFechaFutura() throws Exception {
        String json = """
                {
                    "fecha": "%s",
                    "sucursalCodigo": "SUC00001",
                    "asaltantesCodigos": ["ASS00001"]
                }
                """.formatted(LocalDate.now().plusDays(10));

        mockMvc.perform(post("/api/asaltos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        assertEquals(0, asaltoRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirAsaltoSinSucursal() throws Exception {
        String json = """
                {
                    "fecha": "2024-05-10",
                    "asaltantesCodigos": ["ASS00001"]
                }
                """;

        mockMvc.perform(post("/api/asaltos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirAsaltoConSucursalInexistente() throws Exception {
        String json = """
                {
                    "fecha": "2024-05-10",
                    "sucursalCodigo": "SUC99999",
                    "asaltantesCodigos": ["ASS00001"]
                }
                """;

        mockMvc.perform(post("/api/asaltos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirAsaltoConAsaltanteInexistente() throws Exception {
        String json = """
                {
                    "fecha": "2024-05-10",
                    "sucursalCodigo": "SUC00001",
                    "asaltantesCodigos": ["ASS99999"]
                }
                """;

        mockMvc.perform(post("/api/asaltos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirAsaltoConAsaltanteInactivo() throws Exception {
        asaltante.setActivo(false);
        asaltanteRepository.saveAndFlush(asaltante);

        String json = """
                {
                    "fecha": "2024-05-10",
                    "sucursalCodigo": "SUC00001",
                    "asaltantesCodigos": ["ASS00001"]
                }
                """;

        mockMvc.perform(post("/api/asaltos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirAsaltoConSucursalCerrada() throws Exception {
        sucursal.setActivo(false);
        sucursalRepository.saveAndFlush(sucursal);

        String json = """
                {
                    "fecha": "2024-05-10",
                    "sucursalCodigo": "SUC00001",
                    "asaltantesCodigos": ["ASS00001"]
                }
                """;

        mockMvc.perform(post("/api/asaltos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirAsaltantesDuplicados() throws Exception {
        String json = """
                {
                    "fecha": "2024-05-10",
                    "sucursalCodigo": "SUC00001",
                    "asaltantesCodigos": ["ASS00001", "ASS00001"]
                }
                """;

        mockMvc.perform(post("/api/asaltos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void actualizarSucursalDelAsalto() throws Exception {
        crearAsalto();

        Sucursal nuevaSucursal = new Sucursal();
        nuevaSucursal.setCodigo("SUC00002");
        nuevaSucursal.setDomicilio("Calle Secundaria 456");
        nuevaSucursal.setCantEmpleados(5);
        nuevaSucursal.setEntidadBancaria(sucursal.getEntidadBancaria());
        nuevaSucursal.setActivo(true);
        sucursalRepository.saveAndFlush(nuevaSucursal);

        String json = """
                {
                    "sucursalCodigo": "SUC00002",
                    "asaltantesCodigos": ["ASS00001"]
                }
                """;

        mockMvc.perform(put("/api/asaltos/AST00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucursalCodigo").value("SUC00002"));

        Asalto actualizado = asaltoRepository.findByCodigo("AST00001").get();

        assertEquals("SUC00002", actualizado.getSucursal().getCodigo());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void agregarAsaltanteAlAsalto() throws Exception {
        crearAsalto();

        Asaltante segundo = new Asaltante();
        segundo.setCodigo("ASS00002");
        segundo.setNombre("Pedro Gomez");
        segundo.setActivo(true);
        asaltanteRepository.saveAndFlush(segundo);

        String json = """
                {
                    "asaltantesCodigos": ["ASS00001", "ASS00002"]
                }
                """;

        mockMvc.perform(put("/api/asaltos/AST00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.asaltantesCodigos.length()").value(2));

        Asalto actualizado = asaltoRepository.findByCodigo("AST00001").get();

        assertEquals(2, actualizado.getAsaltantes().size());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void quitarAsaltanteDelAsalto() throws Exception {
        crearAsalto();

        String json = """
                {
                    "asaltantesCodigos": []
                }
                """;

        mockMvc.perform(put("/api/asaltos/AST00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.asaltantesCodigos.length()").value(0));

        Asalto actualizado = asaltoRepository.findByCodigo("AST00001").get();

        assertEquals(0, actualizado.getAsaltantes().size());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void impedirCreacionSinPermisoEscritura() throws Exception {
        String json = """
                {
                    "fecha": "2024-05-10",
                    "sucursalCodigo": "SUC00001",
                    "asaltantesCodigos": ["ASS00001"]
                }
                """;

        mockMvc.perform(post("/api/asaltos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());

        assertEquals(0, asaltoRepository.count());
    }

    @Test
    void impedirAccesoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/asaltos"))
                .andExpect(status().isUnauthorized());
    }
}
