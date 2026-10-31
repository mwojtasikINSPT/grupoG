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
import prog2.policia_backend.models.SentenciaAsaltante;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.AsaltoRepository;
import prog2.policia_backend.repositories.CasoJudicialRepository;
import prog2.policia_backend.repositories.EntidadBancariaRepository;
import prog2.policia_backend.repositories.JuezRepository;
import prog2.policia_backend.repositories.SentenciaAsaltanteRepository;
import prog2.policia_backend.repositories.SucursalRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:sentenciaintegrationtest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SentenciaAsaltanteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AsaltanteRepository asaltanteRepository;

    @Autowired
    private AsaltoRepository asaltoRepository;

    @Autowired
    private CasoJudicialRepository casoJudicialRepository;

    @Autowired
    private SentenciaAsaltanteRepository sentenciaRepository;

    @Autowired
    private JuezRepository juezRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private EntidadBancariaRepository entidadRepository;

    private CasoJudicial caso;
    private Asaltante asaltante;
    private SentenciaAsaltante sentencia;

    @BeforeEach
    void setUp() {
        EntidadBancaria entidad = new EntidadBancaria();
        entidad.setCodigo("EBA00001");
        entidad.setNombre("Banco Nacional");
        entidad.setDomicilioCentral("Buenos Aires");
        entidad.setActivo(true);
        entidad = entidadRepository.saveAndFlush(entidad);

        Sucursal sucursal = new Sucursal();
        sucursal.setCodigo("SUC00001");
        sucursal.setDomicilio("Calle Principal 123");
        sucursal.setCantEmpleados(10);
        sucursal.setEntidadBancaria(entidad);
        sucursal.setActivo(true);
        sucursal = sucursalRepository.saveAndFlush(sucursal);

        Juez juez = new Juez();
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

        Asalto asalto = new Asalto(
                LocalDate.of(2024, 5, 10),
                sucursal,
                participantes
        );

        asalto = asaltoRepository.saveAndFlush(asalto);
        asalto.asignarCodigo("AST00001");
        asalto = asaltoRepository.saveAndFlush(asalto);

        caso = new CasoJudicial("CJU00001", asalto, juez);
        caso = casoJudicialRepository.saveAndFlush(caso);

        sentencia = new SentenciaAsaltante(caso, asaltante);
        sentencia = sentenciaRepository.saveAndFlush(sentencia);
    }

    private String jsonSentencia(boolean condenado, int tiempoCarcel) {
        return """
                {
                    "asaltanteCodigo": "ASS00001",
                    "sentenciado": false,
                    "condenado": %s,
                    "tiempoCarcel": %d
                }
                """.formatted(condenado, tiempoCarcel);
    }

    private void registrarSentencia() throws Exception {
        mockMvc.perform(post("/api/casos-judiciales/CJU00001/sentencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSentencia(true, 5)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarSentenciasPendientesPorCaso() throws Exception {
        mockMvc.perform(get("/api/casos-judiciales/CJU00001/sentencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].asaltanteCodigo").value("ASS00001"))
                .andExpect(jsonPath("$[0].sentenciado").value(false))
                .andExpect(jsonPath("$[0].condenado").isEmpty())
                .andExpect(jsonPath("$[0].tiempoCarcel").isEmpty());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void listarSentenciasPorAsaltante() throws Exception {
        mockMvc.perform(get("/api/casos-judiciales/sentencias/asaltante/ASS00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].asaltanteCodigo").value("ASS00001"));
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void consultarSentenciasDeCasoInexistente() throws Exception {
        mockMvc.perform(get("/api/casos-judiciales/CJU99999/sentencias"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void consultarSentenciasDeAsaltanteInexistente() throws Exception {
        mockMvc.perform(get("/api/casos-judiciales/sentencias/asaltante/ASS99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void registrarSentenciaCondenatoria() throws Exception {
        registrarSentencia();

        SentenciaAsaltante resultado = sentenciaRepository
                .findById(sentencia.getId()).get();

        assertTrue(resultado.isSentenciado());
        assertTrue(resultado.isCondenado());
        assertEquals(5, resultado.getTiempoCarcel());
        assertTrue(casoJudicialRepository.findById(caso.getId()).get().isSentenciado());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void registrarSentenciaAbsolutoria() throws Exception {
        mockMvc.perform(post("/api/casos-judiciales/CJU00001/sentencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSentencia(false, 0)))
                .andExpect(status().isNoContent());

        SentenciaAsaltante resultado = sentenciaRepository
                .findById(sentencia.getId()).get();

        assertTrue(resultado.isSentenciado());
        assertFalse(resultado.isCondenado());
        assertEquals(0, resultado.getTiempoCarcel());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void consultarSentenciaRegistrada() throws Exception {
        sentencia.registrarSentencia(true, 5);
        sentenciaRepository.saveAndFlush(sentencia);

        mockMvc.perform(get("/api/casos-judiciales/CJU00001/sentencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sentenciado").value(true))
                .andExpect(jsonPath("$[0].condenado").value(true))
                .andExpect(jsonPath("$[0].tiempoCarcel").value(5));
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirRegistrarSentenciaDuplicada() throws Exception {
        registrarSentencia();

        mockMvc.perform(post("/api/casos-judiciales/CJU00001/sentencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSentencia(true, 8)))
                .andExpect(status().isConflict());

        assertEquals(1, sentenciaRepository.count());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirCondenaSinTiempoCarcel() throws Exception {
        mockMvc.perform(post("/api/casos-judiciales/CJU00001/sentencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSentencia(true, 0)))
                .andExpect(status().isBadRequest());

        assertFalse(sentenciaRepository.findById(sentencia.getId()).get().isSentenciado());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirAbsolucionConTiempoCarcel() throws Exception {
        mockMvc.perform(post("/api/casos-judiciales/CJU00001/sentencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSentencia(false, 3)))
                .andExpect(status().isBadRequest());

        assertFalse(sentenciaRepository.findById(sentencia.getId()).get().isSentenciado());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirTiempoCarcelNegativo() throws Exception {
        mockMvc.perform(post("/api/casos-judiciales/CJU00001/sentencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSentencia(true, -2)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirSentenciaSinAsaltante() throws Exception {
        String json = """
                {
                    "condenado": true,
                    "tiempoCarcel": 5
                }
                """;

        mockMvc.perform(post("/api/casos-judiciales/CJU00001/sentencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirSentenciaSinCondenado() throws Exception {
        String json = """
                {
                    "asaltanteCodigo": "ASS00001",
                    "tiempoCarcel": 5
                }
                """;

        mockMvc.perform(post("/api/casos-judiciales/CJU00001/sentencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirSentenciaDeAsaltanteInexistente() throws Exception {
        String json = """
                {
                    "asaltanteCodigo": "ASS99999",
                    "sentenciado": false,
                    "condenado": true,
                    "tiempoCarcel": 5
                }
                """;

        mockMvc.perform(post("/api/casos-judiciales/CJU00001/sentencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirSentenciaDeAsaltanteNoParticipante() throws Exception {
        Asaltante otro = new Asaltante();
        otro.setCodigo("ASS00002");
        otro.setNombre("Pedro Lopez");
        otro.setActivo(true);
        asaltanteRepository.saveAndFlush(otro);

        String json = """
                {
                    "asaltanteCodigo": "ASS00002",
                    "sentenciado": false,
                    "condenado": true,
                    "tiempoCarcel": 5
                }
                """;

        mockMvc.perform(post("/api/casos-judiciales/CJU00001/sentencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirRegistrarSentenciaEnCasoInactivo() throws Exception {
        caso.actualizarActivo(false);
        casoJudicialRepository.saveAndFlush(caso);

        mockMvc.perform(post("/api/casos-judiciales/CJU00001/sentencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSentencia(true, 5)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void modificarSentenciaCondenatoria() throws Exception {
        registrarSentencia();

        mockMvc.perform(put("/api/casos-judiciales/CJU00001/sentencias/ASS00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSentencia(true, 10)))
                .andExpect(status().isNoContent());

        SentenciaAsaltante resultado = sentenciaRepository
                .findById(sentencia.getId()).get();

        assertTrue(resultado.isCondenado());
        assertEquals(10, resultado.getTiempoCarcel());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void modificarSentenciaAAbsolucion() throws Exception {
        registrarSentencia();

        mockMvc.perform(put("/api/casos-judiciales/CJU00001/sentencias/ASS00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSentencia(false, 0)))
                .andExpect(status().isNoContent());

        SentenciaAsaltante resultado = sentenciaRepository
                .findById(sentencia.getId()).get();

        assertTrue(resultado.isSentenciado());
        assertFalse(resultado.isCondenado());
        assertEquals(0, resultado.getTiempoCarcel());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirModificarSentenciaPendiente() throws Exception {
        mockMvc.perform(put("/api/casos-judiciales/CJU00001/sentencias/ASS00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSentencia(true, 5)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirModificarSentenciaEnCasoInactivo() throws Exception {
        sentencia.registrarSentencia(true, 5);
        sentenciaRepository.saveAndFlush(sentencia);

        caso.actualizarActivo(false);
        casoJudicialRepository.saveAndFlush(caso);

        mockMvc.perform(put("/api/casos-judiciales/CJU00001/sentencias/ASS00001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSentencia(true, 10)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "LECTURA")
    void impedirRegistroSinPermisoEscritura() throws Exception {
        mockMvc.perform(post("/api/casos-judiciales/CJU00001/sentencias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSentencia(true, 5)))
                .andExpect(status().isForbidden());

        assertFalse(sentenciaRepository.findById(sentencia.getId()).get().isSentenciado());
    }

    @Test
    @WithMockUser(authorities = "ESCRITURA")
    void impedirConsultaSinPermisoLectura() throws Exception {
        mockMvc.perform(get("/api/casos-judiciales/CJU00001/sentencias"))
                .andExpect(status().isForbidden());
    }

    @Test
    void impedirAccesoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/casos-judiciales/CJU00001/sentencias"))
                .andExpect(status().isUnauthorized());
    }
}
