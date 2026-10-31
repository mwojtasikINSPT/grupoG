package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.models.Banda;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:bandatest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BandaRepositoryTest {

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

    private Asaltante crearAsaltante(
            String codigo, String nombre, boolean activo) {

        Asaltante asaltante = new Asaltante();
        asaltante.setCodigo(codigo);
        asaltante.setNombre(nombre);
        asaltante.setActivo(activo);
        asaltante.setBanda(banda);

        return asaltanteRepository.saveAndFlush(asaltante);
    }

    private Banda recargarBanda() {
        entityManager.flush();
        entityManager.clear();

        return bandaRepository.findById(banda.getId()).get();
    }

    @Test
    void guardarBandaCorrectamente() {
        assertNotNull(banda.getId());
        assertEquals("BAN00001", banda.getCodigo());
        assertTrue(banda.isActivo());
    }

    @Test
    void buscarPorIdCorrectamente() {
        Optional<Banda> resultado
                = bandaRepository.findById(banda.getId());

        assertTrue(resultado.isPresent());
        assertEquals("BAN00001", resultado.get().getCodigo());
    }

    @Test
    void buscarPorIdInexistente() {
        assertTrue(bandaRepository.findById(99999L).isEmpty());
    }

    @Test
    void buscarPorCodigoCorrectamente() {
        Optional<Banda> resultado
                = bandaRepository.findByCodigo("BAN00001");

        assertTrue(resultado.isPresent());
        assertEquals(banda.getId(), resultado.get().getId());
    }

    @Test
    void buscarPorCodigoInexistente() {
        assertTrue(bandaRepository.findByCodigo("BAN99999").isEmpty());
    }

    @Test
    void listarBandasActivas() {
        List<Banda> resultado = bandaRepository.findByActivo(true);

        assertEquals(1, resultado.size());
        assertEquals("BAN00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarBandasInactivas() {
        assertTrue(bandaRepository.findByActivo(false).isEmpty());
    }

    @Test
    void actualizarCodigoCorrectamente() {
        banda.setCodigo("BAN00002");
        bandaRepository.saveAndFlush(banda);

        Banda resultado = recargarBanda();

        assertEquals("BAN00002", resultado.getCodigo());
    }

    @Test
    void desactivarBandaCorrectamente() {
        banda.setActivo(false);
        bandaRepository.saveAndFlush(banda);

        Banda resultado = recargarBanda();

        assertFalse(resultado.isActivo());
        assertEquals(1, bandaRepository.findByActivo(false).size());
    }

    @Test
    void reactivarBandaCorrectamente() {
        banda.setActivo(false);
        bandaRepository.saveAndFlush(banda);

        banda.setActivo(true);
        bandaRepository.saveAndFlush(banda);

        Banda resultado = recargarBanda();

        assertTrue(resultado.isActivo());
    }

    @Test
    void bandaSinAsaltantesTieneCeroMiembros() {
        Banda resultado = recargarBanda();

        assertEquals(0, resultado.getCantMiembros());
    }

    @Test
    void contarAsaltantesActivos() {
        crearAsaltante("ASS00001", "Juan Perez", true);
        crearAsaltante("ASS00002", "Pedro Gomez", true);

        Banda resultado = recargarBanda();

        assertEquals(2, resultado.getCantMiembros());
    }

    @Test
    void noContarAsaltantesInactivos() {
        crearAsaltante("ASS00001", "Juan Perez", true);
        crearAsaltante("ASS00002", "Pedro Gomez", false);

        Banda resultado = recargarBanda();

        assertEquals(1, resultado.getCantMiembros());
    }

    @Test
    void actualizarCantidadAlDesactivarAsaltante() {
        Asaltante asaltante
                = crearAsaltante("ASS00001", "Juan Perez", true);

        assertEquals(1, recargarBanda().getCantMiembros());

        asaltante = asaltanteRepository.findById(asaltante.getId()).get();
        asaltante.setActivo(false);
        asaltanteRepository.saveAndFlush(asaltante);

        assertEquals(0, recargarBanda().getCantMiembros());
    }
}
