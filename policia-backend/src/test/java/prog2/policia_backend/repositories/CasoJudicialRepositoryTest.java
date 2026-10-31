package prog2.policia_backend.repositories;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.models.Asalto;
import prog2.policia_backend.models.CasoJudicial;
import prog2.policia_backend.models.EntidadBancaria;
import prog2.policia_backend.models.Juez;
import prog2.policia_backend.models.SentenciaAsaltante;
import prog2.policia_backend.models.Sucursal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:casojudicialtest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CasoJudicialRepositoryTest {

    @Autowired
    private CasoJudicialRepository casoJudicialRepository;

    @Autowired
    private SentenciaAsaltanteRepository sentenciaRepository;

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

    @Autowired
    private EntityManager entityManager;

    private CasoJudicial caso;
    private Juez juez;
    private Asalto asalto;
    private Asaltante asaltante;

    @BeforeEach
    void setUp() {
        EntidadBancaria entidad = new EntidadBancaria();
        entidad.setCodigo("EB00001");
        entidad.setNombre("Banco Nacional");
        entidad.setDomicilioCentral("Buenos Aires");
        entidad.setActivo(true);
        entidad = entidadBancariaRepository.saveAndFlush(entidad);

        Sucursal sucursal = new Sucursal();
        sucursal.setCodigo("SUC00001");
        sucursal.setDomicilio("Corrientes 1234");
        sucursal.setCantEmpleados(20);
        sucursal.setEntidadBancaria(entidad);
        sucursal.setActivo(true);
        sucursal = sucursalRepository.saveAndFlush(sucursal);

        asaltante = new Asaltante();
        asaltante.setCodigo("ASS00001");
        asaltante.setNombre("Juan Perez");
        asaltante.setActivo(true);
        asaltante = asaltanteRepository.saveAndFlush(asaltante);

        List<Asaltante> participantes = new ArrayList<>();
        participantes.add(asaltante);

        asalto = new Asalto(
                LocalDate.of(2026, 10, 1),
                sucursal,
                participantes
        );
        asalto.asignarCodigo("AST00001");
        asalto = asaltoRepository.saveAndFlush(asalto);

        juez = new Juez();
        juez.setCodigo("JUE00001");
        juez.setNombre("Carlos Gomez");
        juez.setJuezDesde(LocalDate.of(2020, 1, 1));
        juez.setActivo(true);
        juez = juezRepository.saveAndFlush(juez);

        caso = new CasoJudicial("CJU00001", asalto, juez);
        caso = casoJudicialRepository.saveAndFlush(caso);
    }

    private CasoJudicial recargarCaso() {
        Long id = caso.getId();

        entityManager.flush();
        entityManager.clear();

        return casoJudicialRepository.findById(id).get();
    }

    private SentenciaAsaltante crearSentencia() {
        SentenciaAsaltante sentencia
                = new SentenciaAsaltante(caso, asaltante);

        return sentenciaRepository.saveAndFlush(sentencia);
    }

    @Test
    void guardarCasoCorrectamente() {
        assertNotNull(caso.getId());
        assertEquals("CJU00001", caso.getCodigo());
        assertTrue(caso.isActivo());
        assertFalse(caso.isSentenciado());
    }

    @Test
    void buscarPorIdCorrectamente() {
        Optional<CasoJudicial> resultado
                = casoJudicialRepository.findById(caso.getId());

        assertTrue(resultado.isPresent());
        assertEquals("CJU00001", resultado.get().getCodigo());
    }

    @Test
    void buscarPorIdInexistente() {
        assertTrue(casoJudicialRepository.findById(99999L).isEmpty());
    }

    @Test
    void buscarPorCodigoCorrectamente() {
        Optional<CasoJudicial> resultado
                = casoJudicialRepository.findByCodigo("CJU00001");

        assertTrue(resultado.isPresent());
        assertEquals(caso.getId(), resultado.get().getId());
    }

    @Test
    void buscarPorCodigoInexistente() {
        assertTrue(casoJudicialRepository
                .findByCodigo("CJU99999").isEmpty());
    }

    @Test
    void listarCasosActivos() {
        List<CasoJudicial> resultado
                = casoJudicialRepository.findByActivo(true);

        assertEquals(1, resultado.size());
        assertEquals("CJU00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarCasosInactivos() {
        assertTrue(casoJudicialRepository.findByActivo(false).isEmpty());
    }

    @Test
    void verificarAsaltoConCasoJudicial() {
        assertTrue(casoJudicialRepository
                .existsByAsalto_Id(asalto.getId()));
    }

    @Test
    void verificarAsaltoSinCasoJudicial() {
        assertFalse(casoJudicialRepository
                .existsByAsalto_Id(99999L));
    }

    @Test
    void verificarJuezConCasoJudicial() {
        assertTrue(casoJudicialRepository
                .existsByJuez_Id(juez.getId()));
    }

    @Test
    void verificarJuezSinCasoJudicial() {
        assertFalse(casoJudicialRepository
                .existsByJuez_Id(99999L));
    }

    @Test
    void listarCasosPorJuez() {
        List<CasoJudicial> resultado
                = casoJudicialRepository.findByJuez_Id(juez.getId());

        assertEquals(1, resultado.size());
        assertEquals("CJU00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarCasosPorJuezInexistente() {
        assertTrue(casoJudicialRepository
                .findByJuez_Id(99999L).isEmpty());
    }

    @Test
    void listarCasosPorAsaltanteConSentencia() {
        crearSentencia();

        List<CasoJudicial> resultado
                = casoJudicialRepository
                        .findBySentencias_Asaltante_Id(asaltante.getId());

        assertEquals(1, resultado.size());
        assertEquals("CJU00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarCasosPorAsaltanteSinSentencia() {
        List<CasoJudicial> resultado
                = casoJudicialRepository
                        .findBySentencias_Asaltante_Id(asaltante.getId());

        assertTrue(resultado.isEmpty());
    }

    @Test
    void listarCasosPorAsaltanteInexistente() {
        crearSentencia();

        assertTrue(casoJudicialRepository
                .findBySentencias_Asaltante_Id(99999L).isEmpty());
    }

    @Test
    void actualizarJuezCorrectamente() {
        Juez nuevoJuez = new Juez();
        nuevoJuez.setCodigo("JUE00002");
        nuevoJuez.setNombre("Pedro Lopez");
        nuevoJuez.setJuezDesde(LocalDate.of(2022, 1, 1));
        nuevoJuez.setActivo(true);
        nuevoJuez = juezRepository.saveAndFlush(nuevoJuez);

        Long nuevoJuezId = nuevoJuez.getId();

        caso.cambiarJuez(nuevoJuez);
        casoJudicialRepository.saveAndFlush(caso);

        CasoJudicial resultado = recargarCaso();

        assertEquals(nuevoJuezId, resultado.getJuez().getId());
        assertTrue(casoJudicialRepository
                .findByJuez_Id(juez.getId()).isEmpty());
    }

    @Test
    void actualizarEstadoSentenciado() {
        caso.actualizarEstadoSentenciado(true);
        casoJudicialRepository.saveAndFlush(caso);

        CasoJudicial resultado = recargarCaso();

        assertTrue(resultado.isSentenciado());
    }

    @Test
    void desactivarCasoCorrectamente() {
        caso.actualizarActivo(false);
        casoJudicialRepository.saveAndFlush(caso);

        CasoJudicial resultado = recargarCaso();

        assertFalse(resultado.isActivo());
        assertEquals(1,
                casoJudicialRepository.findByActivo(false).size());
    }

    @Test
    void reactivarCasoCorrectamente() {
        caso.actualizarActivo(false);
        casoJudicialRepository.saveAndFlush(caso);

        caso.actualizarActivo(true);
        casoJudicialRepository.saveAndFlush(caso);

        assertTrue(recargarCaso().isActivo());
    }
}
