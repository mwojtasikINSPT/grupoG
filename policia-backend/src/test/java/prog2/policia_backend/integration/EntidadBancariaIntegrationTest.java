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

import prog2.policia_backend.models.EntidadBancaria;
import prog2.policia_backend.models.MotivoBajaEntidadBancaria;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.repositories.EntidadBancariaRepository;
import prog2.policia_backend.repositories.SucursalRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:entidadbancariaintegrationtest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class EntidadBancariaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntidadBancariaRepository entidadRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    private EntidadBancaria entidad;

    @BeforeEach
    void setUp() {
        entidad = new EntidadBancaria();
        entidad.setCodigo("EBA00001");
        entidad.setNombre("Banco Nacional");
        entidad.setDomicilioCentral("Buenos Aires");
        entidad.setActivo(true);

        entidad = entidadRepository.saveAndFlush(entidad);
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarEntidadesCorrectamente() throws Exception {
        mockMvc.perform(get("/api/entidades-bancarias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("EBA00001"))
                .andExpect(jsonPath("$[0].nombre").value("Banco Nacional"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarEntidadPorCodigo() throws Exception {
        mockMvc.perform(get("/api/entidades-bancarias/EBA00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("EBA00001"))
                .andExpect(jsonPath("$.domicilioCentral").value("Buenos Aires"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarEntidadInexistente() throws Exception {
        mockMvc.perform(get("/api/entidades-bancarias/EBA99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void filtrarEntidadesActivas() throws Exception {
        mockMvc.perform(get("/api/entidades-bancarias")
                .param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarEntidadPorNombre() throws Exception {
        mockMvc.perform(get("/api/entidades-bancarias")
                .param("nombre", "nacional"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("EBA00001"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarEntidadPorDomicilio() throws Exception {
        mockMvc.perform(get("/api/entidades-bancarias")
                .param("domicilio", "aires"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("EBA00001"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarSucursalesDeEntidadSinSucursales() throws Exception {
        mockMvc.perform(get("/api/entidades-bancarias/EBA00001/sucursales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void crearEntidadCorrectamente() throws Exception {
        String json = """
                {
                    "nombre": "banco federal",
                    "domicilioCentral": "cordoba capital"
                }
                """;

        mockMvc.perform(post("/api/entidades-bancarias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").exists())
                .andExpect(jsonPath("$.nombre").value("Banco Federal"))
                .andExpect(jsonPath("$.activo").value(true));

        assertEquals(2, entidadRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirEntidadSinNombre() throws Exception {
        String json = """
                {
                    "nombre": "",
                    "domicilioCentral": "Buenos Aires"
                }
                """;

        mockMvc.perform(post("/api/entidades-bancarias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        assertEquals(1, entidadRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void actualizarEntidadCorrectamente() throws Exception {
        String json = """
                {
                    "nombre": "banco federal",
                    "domicilioCentral": "cordoba capital"
                }
                """;

        mockMvc.perform(put("/api/entidades-bancarias/EBA00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Banco Federal"))
                .andExpect(jsonPath("$.domicilioCentral").value("Cordoba Capital"));

        EntidadBancaria actualizada
                = entidadRepository.findByCodigo("EBA00001").get();

        assertEquals("Banco Federal", actualizada.getNombre());
        assertEquals("Cordoba Capital", actualizada.getDomicilioCentral());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void darDeBajaEntidadCorrectamente() throws Exception {
        String json = """
                {
                    "motivoBaja": "FUSION"
                }
                """;

        mockMvc.perform(patch("/api/entidades-bancarias/EBA00001/baja")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent());

        EntidadBancaria resultado
                = entidadRepository.findByCodigo("EBA00001").get();

        assertFalse(resultado.isActivo());
        assertEquals(MotivoBajaEntidadBancaria.FUSION,
                resultado.getMotivoBaja());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirBajaSinMotivo() throws Exception {
        mockMvc.perform(patch("/api/entidades-bancarias/EBA00001/baja")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isConflict());

        assertTrue(entidadRepository
                .findByCodigo("EBA00001").get().isActivo());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirBajaConSucursalActiva() throws Exception {
        Sucursal sucursal = new Sucursal();
        sucursal.setCodigo("SUC00001");
        sucursal.setDomicilio("Corrientes 1234");
        sucursal.setCantEmpleados(20);
        sucursal.setEntidadBancaria(entidad);
        sucursal.setActivo(true);
        sucursalRepository.saveAndFlush(sucursal);

        String json = """
                {
                    "motivoBaja": "CIERRE_DEFINITIVO"
                }
                """;

        mockMvc.perform(patch("/api/entidades-bancarias/EBA00001/baja")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());

        assertTrue(entidadRepository
                .findByCodigo("EBA00001").get().isActivo());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void reactivarEntidadCorrectamente() throws Exception {
        entidad.setActivo(false);
        entidad.setMotivoBaja(MotivoBajaEntidadBancaria.FUSION);
        entidadRepository.saveAndFlush(entidad);

        mockMvc.perform(patch("/api/entidades-bancarias/EBA00001/reactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));

        EntidadBancaria resultado
                = entidadRepository.findByCodigo("EBA00001").get();

        assertTrue(resultado.isActivo());
        assertNull(resultado.getMotivoBaja());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirReactivacionDeEntidadActiva() throws Exception {
        mockMvc.perform(patch("/api/entidades-bancarias/EBA00001/reactivar"))
                .andExpect(status().isConflict());

        assertTrue(entidadRepository
                .findByCodigo("EBA00001").get().isActivo());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void impedirCreacionSinPermisoEscritura() throws Exception {
        String json = """
                {
                    "nombre": "Banco Federal",
                    "domicilioCentral": "Cordoba"
                }
                """;

        mockMvc.perform(post("/api/entidades-bancarias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());

        assertEquals(1, entidadRepository.count());
    }

    @Test
    void impedirAccesoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/entidades-bancarias"))
                .andExpect(status().isUnauthorized());
    }
}
