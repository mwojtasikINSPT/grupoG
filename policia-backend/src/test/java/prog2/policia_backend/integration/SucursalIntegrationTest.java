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
import prog2.policia_backend.models.MotivoCierreSucursal;
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
    "spring.datasource.url=jdbc:h2:mem:sucursalintegrationtest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SucursalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private EntidadBancariaRepository entidadRepository;

    private Sucursal sucursal;
    private EntidadBancaria entidad;

    @BeforeEach
    void setUp() {
        entidad = new EntidadBancaria();
        entidad.setCodigo("EB00001");
        entidad.setNombre("Banco Nacional");
        entidad.setDomicilioCentral("Buenos Aires");
        entidad.setActivo(true);
        entidad = entidadRepository.saveAndFlush(entidad);

        sucursal = new Sucursal();
        sucursal.setCodigo("SUC00001");
        sucursal.setDomicilio("Avenida Corrientes 1234");
        sucursal.setCantEmpleados(20);
        sucursal.setEntidadBancaria(entidad);
        sucursal.setActivo(true);
        sucursal = sucursalRepository.saveAndFlush(sucursal);
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarSucursalesCorrectamente() throws Exception {
        mockMvc.perform(get("/api/sucursales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("SUC00001"))
                .andExpect(jsonPath("$[0].cantEmpleados").value(20));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarSucursalPorCodigo() throws Exception {
        mockMvc.perform(get("/api/sucursales/SUC00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.domicilio")
                        .value("Avenida Corrientes 1234"))
                .andExpect(jsonPath("$.entidadBancariaCodigo")
                        .value("EB00001"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarSucursalInexistente() throws Exception {
        mockMvc.perform(get("/api/sucursales/SUC99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void filtrarSucursalesActivas() throws Exception {
        mockMvc.perform(get("/api/sucursales")
                .param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarSucursalPorDomicilio() throws Exception {
        mockMvc.perform(get("/api/sucursales")
                .param("domicilio", "corrientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("SUC00001"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarContratosDeSucursalSinContratos() throws Exception {
        mockMvc.perform(get("/api/sucursales/SUC00001/contratos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarAsaltosDeSucursalSinAsaltos() throws Exception {
        mockMvc.perform(get("/api/sucursales/SUC00001/asaltos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void crearSucursalCorrectamente() throws Exception {
        String json = """
                {
                    "domicilio": "avenida santa fe 2000",
                    "cantEmpleados": 15,
                    "entidadBancariaCodigo": "EB00001"
                }
                """;

        mockMvc.perform(post("/api/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").exists())
                .andExpect(jsonPath("$.domicilio")
                        .value("Avenida Santa Fe 2000"))
                .andExpect(jsonPath("$.activo").value(true));

        assertEquals(2, sucursalRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirSucursalConEntidadInactiva() throws Exception {
        entidad.setActivo(false);
        entidadRepository.saveAndFlush(entidad);

        String json = """
                {
                    "domicilio": "Avenida Santa Fe 2000",
                    "cantEmpleados": 15,
                    "entidadBancariaCodigo": "EB00001"
                }
                """;

        mockMvc.perform(post("/api/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());

        assertEquals(1, sucursalRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void actualizarSucursalCorrectamente() throws Exception {
        String json = """
                {
                    "domicilio": "avenida cordoba 1500",
                    "cantEmpleados": 35
                }
                """;

        mockMvc.perform(put("/api/sucursales/SUC00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.domicilio")
                        .value("Avenida Cordoba 1500"))
                .andExpect(jsonPath("$.cantEmpleados").value(35));

        Sucursal actualizada
                = sucursalRepository.findByCodigo("SUC00001").get();

        assertEquals("Avenida Cordoba 1500", actualizada.getDomicilio());
        assertEquals(35, actualizada.getCantEmpleados());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void darDeBajaSucursalCorrectamente() throws Exception {
        String json = """
                {
                    "motivoCierre": "CIERRE_TEMPORAL"
                }
                """;

        mockMvc.perform(patch("/api/sucursales/SUC00001/baja")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent());

        Sucursal resultado
                = sucursalRepository.findByCodigo("SUC00001").get();

        assertFalse(resultado.isActivo());
        assertEquals(MotivoCierreSucursal.CIERRE_TEMPORAL,
                resultado.getMotivoCierre());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirBajaSinMotivo() throws Exception {
        mockMvc.perform(patch("/api/sucursales/SUC00001/baja")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isConflict());

        assertTrue(sucursalRepository
                .findByCodigo("SUC00001").get().isActivo());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void reactivarSucursalCorrectamente() throws Exception {
        sucursal.setActivo(false);
        sucursal.setMotivoCierre(MotivoCierreSucursal.CIERRE_TEMPORAL);
        sucursalRepository.saveAndFlush(sucursal);

        mockMvc.perform(patch("/api/sucursales/SUC00001/reactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));

        Sucursal resultado
                = sucursalRepository.findByCodigo("SUC00001").get();

        assertTrue(resultado.isActivo());
        assertNull(resultado.getMotivoCierre());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirReactivacionDeCierreDefinitivo() throws Exception {
        sucursal.setActivo(false);
        sucursal.setMotivoCierre(MotivoCierreSucursal.CIERRE_DEFINITIVO);
        sucursalRepository.saveAndFlush(sucursal);

        mockMvc.perform(patch("/api/sucursales/SUC00001/reactivar"))
                .andExpect(status().isConflict());

        assertFalse(sucursalRepository
                .findByCodigo("SUC00001").get().isActivo());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void impedirCreacionSinPermisoEscritura() throws Exception {
        String json = """
                {
                    "domicilio": "Avenida Santa Fe 2000",
                    "cantEmpleados": 15,
                    "entidadBancariaCodigo": "EB00001"
                }
                """;

        mockMvc.perform(post("/api/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());

        assertEquals(1, sucursalRepository.count());
    }
}
