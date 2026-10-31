package prog2.policia_backend.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import prog2.policia_backend.models.Juez;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:jueztest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class JuezRepositoryTest {

    @Autowired
    private JuezRepository juezRepository;

    private Juez juez;

    @BeforeEach
    void setUp() {
        juez = new Juez();
        juez.setCodigo("JUE00001");
        juez.setNombre("Juan Perez");
        juez.setJuezDesde(LocalDate.of(2020, 3, 15));
        juez.setActivo(true);

        juez = juezRepository.saveAndFlush(juez);
    }

    @Test
    void guardarJuezCorrectamente() {
        assertNotNull(juez.getId());
        assertEquals("JUE00001", juez.getCodigo());
        assertEquals("Juan Perez", juez.getNombre());
        assertTrue(juez.isActivo());
    }

    @Test
    void buscarPorIdCorrectamente() {
        Optional<Juez> resultado
                = juezRepository.findById(juez.getId());

        assertTrue(resultado.isPresent());
        assertEquals("Juan Perez", resultado.get().getNombre());
    }

    @Test
    void buscarPorIdInexistente() {
        assertTrue(juezRepository.findById(99999L).isEmpty());
    }

    @Test
    void buscarPorCodigoCorrectamente() {
        Optional<Juez> resultado
                = juezRepository.findByCodigo("JUE00001");

        assertTrue(resultado.isPresent());
        assertEquals("Juan Perez", resultado.get().getNombre());
    }

    @Test
    void buscarPorCodigoInexistente() {
        assertTrue(juezRepository.findByCodigo("JUE99999").isEmpty());
    }

    @Test
    void listarJuecesActivos() {
        List<Juez> resultado = juezRepository.findByActivo(true);

        assertEquals(1, resultado.size());
        assertEquals("JUE00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarJuecesInactivos() {
        assertTrue(juezRepository.findByActivo(false).isEmpty());
    }

    @Test
    void buscarPorNombreParcial() {
        List<Juez> resultado
                = juezRepository.findByNombreContainingIgnoreCase("Juan");

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorNombreSinDistinguirMayusculas() {
        List<Juez> resultado
                = juezRepository.findByNombreContainingIgnoreCase("juan perez");

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorNombreInexistente() {
        assertTrue(juezRepository
                .findByNombreContainingIgnoreCase("Carlos").isEmpty());
    }

    @Test
    void actualizarNombreCorrectamente() {
        juez.setNombre("Pedro Gomez");
        juezRepository.saveAndFlush(juez);

        Juez resultado
                = juezRepository.findById(juez.getId()).get();

        assertEquals("Pedro Gomez", resultado.getNombre());
    }

    @Test
    void actualizarFechaJuezDesde() {
        LocalDate nuevaFecha = LocalDate.of(2022, 6, 10);
        juez.setJuezDesde(nuevaFecha);
        juezRepository.saveAndFlush(juez);

        Juez resultado
                = juezRepository.findById(juez.getId()).get();

        assertEquals(nuevaFecha, resultado.getJuezDesde());
    }

    @Test
    void desactivarJuezCorrectamente() {
        juez.setActivo(false);
        juezRepository.saveAndFlush(juez);

        List<Juez> resultado = juezRepository.findByActivo(false);

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).isActivo());
    }

    @Test
    void reactivarJuezCorrectamente() {
        juez.setActivo(false);
        juezRepository.saveAndFlush(juez);

        juez.setActivo(true);
        juezRepository.saveAndFlush(juez);

        Juez resultado
                = juezRepository.findById(juez.getId()).get();

        assertTrue(resultado.isActivo());
    }

    @Test
    void eliminarJuezCorrectamente() {
        Long id = juez.getId();

        juezRepository.delete(juez);
        juezRepository.flush();

        assertTrue(juezRepository.findById(id).isEmpty());
    }

    @Test
    void buscarPorNombreYEstadoActivo() {
        List<Juez> resultado = juezRepository.findByNombreContainingIgnoreCaseAndActivo("juan", true);

        assertEquals(1, resultado.size());
        assertEquals("JUE00001", resultado.get(0).getCodigo());
    }

    @Test
    void buscarPorNombreYEstadoInactivo() {
        List<Juez> resultado = juezRepository.findByNombreContainingIgnoreCaseAndActivo("juan", false);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorNombreYEstadoDespuesDeDesactivar() {
        juez.setActivo(false);
        juezRepository.saveAndFlush(juez);

        List<Juez> resultado = juezRepository.findByNombreContainingIgnoreCaseAndActivo("JUAN", false);

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).isActivo());
    }
}
