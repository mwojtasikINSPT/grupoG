package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;

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
    "spring.datasource.url=jdbc:h2:mem:asaltantetest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AsaltanteRepositoryTest {

    @Autowired
    private AsaltanteRepository asaltanteRepository;

    @Autowired
    private BandaRepository bandaRepository;

    private Asaltante asaltante;
    private Banda banda;

    @BeforeEach
    void setUp() {
        banda = new Banda();
        banda.setCodigo("BAN00001");
        banda.setActivo(true);
        banda = bandaRepository.saveAndFlush(banda);

        asaltante = new Asaltante();
        asaltante.setCodigo("ASS00001");
        asaltante.setNombre("Juan Perez");
        asaltante.setActivo(true);
        asaltante.setBanda(banda);

        asaltante = asaltanteRepository.saveAndFlush(asaltante);
    }

    @Test
    void guardarAsaltanteCorrectamente() {
        assertNotNull(asaltante.getId());
        assertEquals("ASS00001", asaltante.getCodigo());
        assertEquals("Juan Perez", asaltante.getNombre());
        assertTrue(asaltante.isActivo());
    }

    @Test
    void buscarPorIdCorrectamente() {
        Optional<Asaltante> resultado
                = asaltanteRepository.findById(asaltante.getId());

        assertTrue(resultado.isPresent());
        assertEquals("ASS00001", resultado.get().getCodigo());
    }

    @Test
    void buscarPorIdInexistente() {
        assertTrue(asaltanteRepository.findById(99999L).isEmpty());
    }

    @Test
    void buscarPorCodigoCorrectamente() {
        Optional<Asaltante> resultado
                = asaltanteRepository.findByCodigo("ASS00001");

        assertTrue(resultado.isPresent());
        assertEquals("Juan Perez", resultado.get().getNombre());
    }

    @Test
    void buscarPorCodigoInexistente() {
        assertTrue(asaltanteRepository.findByCodigo("ASS99999").isEmpty());
    }

    @Test
    void listarAsaltantesActivos() {
        List<Asaltante> resultado
                = asaltanteRepository.findByActivo(true);

        assertEquals(1, resultado.size());
        assertEquals("ASS00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarAsaltantesInactivos() {
        assertTrue(asaltanteRepository.findByActivo(false).isEmpty());
    }

    @Test
    void buscarPorNombreParcial() {
        List<Asaltante> resultado
                = asaltanteRepository.findByNombreContainingIgnoreCase("Juan");

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorNombreSinDistinguirMayusculas() {
        List<Asaltante> resultado
                = asaltanteRepository.findByNombreContainingIgnoreCase("juan perez");

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorNombreInexistente() {
        assertTrue(asaltanteRepository
                .findByNombreContainingIgnoreCase("Carlos").isEmpty());
    }

    @Test
    void verificarBandaConAsaltanteActivo() {
        assertTrue(asaltanteRepository
                .existsByBanda_IdAndActivoTrue(banda.getId()));
    }

    @Test
    void verificarBandaSinAsaltantesActivos() {
        asaltante.setActivo(false);
        asaltanteRepository.saveAndFlush(asaltante);

        assertFalse(asaltanteRepository
                .existsByBanda_IdAndActivoTrue(banda.getId()));
    }

    @Test
    void verificarBandaInexistenteSinAsaltantes() {
        assertFalse(asaltanteRepository
                .existsByBanda_IdAndActivoTrue(99999L));
    }

    @Test
    void actualizarNombreCorrectamente() {
        asaltante.setNombre("Pedro Gomez");
        asaltanteRepository.saveAndFlush(asaltante);

        Asaltante resultado
                = asaltanteRepository.findById(asaltante.getId()).get();

        assertEquals("Pedro Gomez", resultado.getNombre());
    }

    @Test
    void quitarAsaltanteDeBanda() {
        asaltante.setBanda(null);
        asaltanteRepository.saveAndFlush(asaltante);

        Asaltante resultado
                = asaltanteRepository.findById(asaltante.getId()).get();

        assertNull(resultado.getBanda());
        assertFalse(asaltanteRepository
                .existsByBanda_IdAndActivoTrue(banda.getId()));
    }

    @Test
    void desactivarAsaltanteCorrectamente() {
        asaltante.setActivo(false);
        asaltanteRepository.saveAndFlush(asaltante);

        List<Asaltante> resultado
                = asaltanteRepository.findByActivo(false);

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).isActivo());
    }

    @Test
    void buscarPorNombreYEstadoActivo() {
        List<Asaltante> resultado = asaltanteRepository.findByNombreContainingIgnoreCaseAndActivo("juan", true);

        assertEquals(1, resultado.size());
        assertEquals("ASS00001", resultado.get(0).getCodigo());
    }

    @Test
    void buscarPorNombreYEstadoInactivo() {
        List<Asaltante> resultado = asaltanteRepository.findByNombreContainingIgnoreCaseAndActivo("juan", false);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorNombreYEstadoDespuesDeDesactivar() {
        asaltante.setActivo(false);
        asaltanteRepository.saveAndFlush(asaltante);

        List<Asaltante> resultado = asaltanteRepository.findByNombreContainingIgnoreCaseAndActivo("JUAN", false);

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).isActivo());
    }
}
