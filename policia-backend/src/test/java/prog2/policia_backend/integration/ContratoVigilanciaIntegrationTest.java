package prog2.policia_backend.integration;

import java.time.LocalDate;
import java.util.EnumSet;

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

import prog2.policia_backend.models.ContratoVigilancia;
import prog2.policia_backend.models.EntidadBancaria;
import prog2.policia_backend.models.MotivoBajaContrato;
import prog2.policia_backend.models.Permiso;
import prog2.policia_backend.models.Rol;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.models.Vigilante;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;
import prog2.policia_backend.repositories.EntidadBancariaRepository;
import prog2.policia_backend.repositories.RolRepository;
import prog2.policia_backend.repositories.SucursalRepository;
import prog2.policia_backend.repositories.VigilanteRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:contratointegrationtest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ContratoVigilanciaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContratoVigilanciaRepository contratoRepository;

    @Autowired
    private VigilanteRepository vigilanteRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private EntidadBancariaRepository entidadRepository;

    @Autowired
    private RolRepository rolRepository;

    private ContratoVigilancia contrato;
    private Vigilante vigilante;
    private Sucursal sucursal;

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
        vigilante.setPassword("clave123");
        vigilante.setEdad(30);
        vigilante.setRol(rol);
        vigilante.setActivo(true);
        vigilante = vigilanteRepository.saveAndFlush(vigilante);

        EntidadBancaria entidad = new EntidadBancaria();
        entidad.setCodigo("EB00001");
        entidad.setNombre("Banco Nacional");
        entidad.setDomicilioCentral("Buenos Aires");
        entidad.setActivo(true);
        entidad = entidadRepository.saveAndFlush(entidad);

        sucursal = new Sucursal();
        sucursal.setCodigo("SUC00001");
        sucursal.setDomicilio("Corrientes 1234");
        sucursal.setCantEmpleados(20);
        sucursal.setEntidadBancaria(entidad);
        sucursal.setActivo(true);
        sucursal = sucursalRepository.saveAndFlush(sucursal);

        contrato = new ContratoVigilancia();
        contrato.setCodigo("CDV00001");
        contrato.setFecha(LocalDate.now().plusDays(10));
        contrato.setConArma(true);
        contrato.setVigilante(vigilante);
        contrato.setSucursal(sucursal);
        contrato.setActivo(true);
        contrato = contratoRepository.saveAndFlush(contrato);
    }

    private String crearJson(String fecha, String vigilanteCodigo) {
        return """
                {
                    "fecha": "%s",
                    "conArma": false,
                    "vigilanteCodigo": "%s",
                    "sucursalCodigo": "SUC00001"
                }
                """.formatted(fecha, vigilanteCodigo);
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarContratosCorrectamente() throws Exception {
        mockMvc.perform(get("/api/contratos-vigilancia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("CDV00001"))
                .andExpect(jsonPath("$[0].conArma").value(true));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarContratoPorCodigo() throws Exception {
        mockMvc.perform(get("/api/contratos-vigilancia/CDV00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vigilanteCodigo").value("VIG00001"))
                .andExpect(jsonPath("$.sucursalCodigo").value("SUC00001"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarContratosActivos() throws Exception {
        mockMvc.perform(get("/api/contratos-vigilancia")
                .param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void buscarPorNombreVigilante() throws Exception {
        mockMvc.perform(get("/api/contratos-vigilancia")
                .param("vigilante", "juan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("CDV00001"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarContratosPorVigilante() throws Exception {
        mockMvc.perform(get("/api/contratos-vigilancia/vigilante/VIG00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("CDV00001"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarContratosPorSucursal() throws Exception {
        mockMvc.perform(get("/api/contratos-vigilancia/sucursal/SUC00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("CDV00001"));
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void crearContratoCorrectamente() throws Exception {
        String fecha = LocalDate.now().plusDays(20).toString();
        String json = crearJson(fecha, "VIG00001");

        mockMvc.perform(post("/api/contratos-vigilancia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").exists())
                .andExpect(jsonPath("$.conArma").value(false))
                .andExpect(jsonPath("$.activo").value(true));

        assertEquals(2, contratoRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirContratoDuplicado() throws Exception {
        String fecha = contrato.getFecha().toString();
        String json = crearJson(fecha, "VIG00001");

        mockMvc.perform(post("/api/contratos-vigilancia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());

        assertEquals(1, contratoRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirContratoConFechaPasada() throws Exception {
        String fecha = LocalDate.now().minusDays(5).toString();
        String json = crearJson(fecha, "VIG00001");

        mockMvc.perform(post("/api/contratos-vigilancia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        assertEquals(1, contratoRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void actualizarContratoCorrectamente() throws Exception {
        String json = """
                {
                    "conArma": false
                }
                """;

        mockMvc.perform(put("/api/contratos-vigilancia/CDV00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conArma").value(false));

        ContratoVigilancia actualizado
                = contratoRepository.findByCodigo("CDV00001").get();

        assertFalse(actualizado.getConArma());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void darDeBajaContratoCorrectamente() throws Exception {
        mockMvc.perform(patch("/api/contratos-vigilancia/CDV00001/baja"))
                .andExpect(status().isNoContent());

        ContratoVigilancia resultado
                = contratoRepository.findByCodigo("CDV00001").get();

        assertFalse(resultado.getActivo());
        assertEquals(MotivoBajaContrato.CANCELACION,
                resultado.getMotivoBaja());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirBajaDeContratoInactivo() throws Exception {
        contrato.setActivo(false);
        contratoRepository.saveAndFlush(contrato);

        mockMvc.perform(patch("/api/contratos-vigilancia/CDV00001/baja"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirContratoConVigilanteInactivo() throws Exception {
        vigilante.setActivo(false);
        vigilanteRepository.saveAndFlush(vigilante);

        String json = crearJson(
                LocalDate.now().plusDays(20).toString(),
                "VIG00001");

        mockMvc.perform(post("/api/contratos-vigilancia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());

        assertEquals(1, contratoRepository.count());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void impedirCreacionSinPermisoEscritura() throws Exception {
        String json = crearJson(
                LocalDate.now().plusDays(20).toString(),
                "VIG00001");

        mockMvc.perform(post("/api/contratos-vigilancia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());

        assertEquals(1, contratoRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirLecturaSinPermisoLectura() throws Exception {
        mockMvc.perform(get("/api/contratos-vigilancia"))
                .andExpect(status().isForbidden());
    }

    @Test
    void impedirAccesoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/contratos-vigilancia"))
                .andExpect(status().isUnauthorized());
    }
}
