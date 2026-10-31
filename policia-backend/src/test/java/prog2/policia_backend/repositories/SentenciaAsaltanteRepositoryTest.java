
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
    "spring.datasource.url=jdbc:h2:mem:sentenciarepositorytest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SentenciaAsaltanteRepositoryTest {

    @Autowired
    private SentenciaAsaltanteRepository sentenciaRepository;

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

    @Autowired
    private EntityManager entityManager;

    private CasoJudicial caso;
    private Asaltante asaltante;
    private SentenciaAsaltante sentencia;

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

        Asalto asalto = new Asalto(
                LocalDate.of(2026, 10, 1),
                sucursal,
                participantes
        );
        asalto.asignarCodigo("AST00001");
        asalto = asaltoRepository.saveAndFlush(asalto);

        Juez juez = new Juez();
        juez.setCodigo("JUE00001");
        juez.setNombre("Carlos Gomez");
        juez.setJuezDesde(LocalDate.of(2020, 1, 1));
        juez.setActivo(true);
        juez = juezRepository.saveAndFlush(juez);

        caso = new CasoJudicial("CJU00001", asalto, juez);
        caso = casoJudicialRepository.saveAndFlush(caso);

        sentencia = new SentenciaAsaltante(caso, asaltante);
        sentencia = sentenciaRepository.saveAndFlush(sentencia);
    }

    private SentenciaAsaltante recargarSentencia() {
        Long id = sentencia.getId();

        entityManager.flush();
        entityManager.clear();

        return sentenciaRepository.findById(id).get();
    }

    @Test
    void guardarSentenciaCorrectamente() {
        assertNotNull(sentencia.getId());
        assertEquals(caso.getId(), sentencia.getCasoJudicial().getId());
        assertEquals(asaltante.getId(), sentencia.getAsaltante().getId());
    }

    @Test
    void sentenciaInicialmenteNoSentenciada() {
        SentenciaAsaltante resultado = recargarSentencia();

        assertFalse(resultado.isSentenciado());
        assertFalse(resultado.isCondenado());
        assertEquals(0, resultado.getTiempoCarcel());
    }

    @Test
    void buscarPorIdCorrectamente() {
        Optional<SentenciaAsaltante> resultado =
                sentenciaRepository.findById(sentencia.getId());

        assertTrue(resultado.isPresent());
        assertEquals(sentencia.getId(), resultado.get().getId());
    }

    @Test
    void buscarPorIdInexistente() {
        assertTrue(sentenciaRepository.findById(99999L).isEmpty());
    }

    @Test
    void listarSentenciasPorCaso() {
        List<SentenciaAsaltante> resultado =
                sentenciaRepository.findByCasoJudicial_Id(caso.getId());

        assertEquals(1, resultado.size());
        assertEquals(asaltante.getId(),
                resultado.get(0).getAsaltante().getId());
    }

    @Test
    void listarSentenciasPorCasoInexistente() {
        assertTrue(sentenciaRepository
                .findByCasoJudicial_Id(99999L).isEmpty());
    }

    @Test
    void listarSentenciasPorAsaltante() {
        List<SentenciaAsaltante> resultado =
                sentenciaRepository.findByAsaltante_Id(asaltante.getId());

        assertEquals(1, resultado.size());
        assertEquals(caso.getId(),
                resultado.get(0).getCasoJudicial().getId());
    }

    @Test
    void listarSentenciasPorAsaltanteInexistente() {
        assertTrue(sentenciaRepository
                .findByAsaltante_Id(99999L).isEmpty());
    }

    @Test
    void buscarPorCasoYAsaltanteCorrectamente() {
        Optional<SentenciaAsaltante> resultado =
                sentenciaRepository.findByCasoJudicial_IdAndAsaltante_Id(
                        caso.getId(), asaltante.getId());

        assertTrue(resultado.isPresent());
        assertEquals(sentencia.getId(), resultado.get().getId());
    }

    @Test
    void buscarPorCasoYAsaltanteInexistentes() {
        assertTrue(sentenciaRepository
                .findByCasoJudicial_IdAndAsaltante_Id(
                        99999L, 99999L).isEmpty());
    }

    @Test
    void verificarSentenciaExistente() {
        assertTrue(sentenciaRepository
                .existsByCasoJudicial_IdAndAsaltante_Id(
                        caso.getId(), asaltante.getId()));
    }

    @Test
    void verificarSentenciaInexistente() {
        assertFalse(sentenciaRepository
                .existsByCasoJudicial_IdAndAsaltante_Id(
                        caso.getId(), 99999L));
    }

    @Test
    void registrarCondenaCorrectamente() {
        sentencia.registrarSentencia(true, 5);
        sentenciaRepository.saveAndFlush(sentencia);

        SentenciaAsaltante resultado = recargarSentencia();

        assertTrue(resultado.isSentenciado());
        assertTrue(resultado.isCondenado());
        assertEquals(5, resultado.getTiempoCarcel());
    }

    @Test
    void registrarAbsolucionCorrectamente() {
        sentencia.registrarSentencia(false, 0);
        sentenciaRepository.saveAndFlush(sentencia);

        SentenciaAsaltante resultado = recargarSentencia();

        assertTrue(resultado.isSentenciado());
        assertFalse(resultado.isCondenado());
        assertEquals(0, resultado.getTiempoCarcel());
    }

    @Test
    void modificarSentenciaCorrectamente() {
        sentencia.registrarSentencia(true, 5);
        sentenciaRepository.saveAndFlush(sentencia);

        sentencia.modificarSentencia(true, 10);
        sentenciaRepository.saveAndFlush(sentencia);

        SentenciaAsaltante resultado = recargarSentencia();

        assertTrue(resultado.isSentenciado());
        assertTrue(resultado.isCondenado());
        assertEquals(10, resultado.getTiempoCarcel());
    }

    @Test
    void eliminarSentenciaCorrectamente() {
        Long id = sentencia.getId();

        sentenciaRepository.delete(sentencia);
        sentenciaRepository.flush();

        assertTrue(sentenciaRepository.findById(id).isEmpty());
        assertFalse(sentenciaRepository
                .existsByCasoJudicial_IdAndAsaltante_Id(
                        caso.getId(), asaltante.getId()));
    }
}
