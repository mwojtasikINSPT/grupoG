
package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import prog2.policia_backend.models.Rol;
import prog2.policia_backend.models.Vigilante;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:vigilantetest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class VigilanteRepositoryTest {

    @Autowired
    private VigilanteRepository vigilanteRepository;

    @Autowired
    private RolRepository rolRepository;

    private Vigilante vigilante;

    @BeforeEach
    void setUp() {
        Rol rol = new Rol();
        rol.setCodigo("ROL00001");
        rol.setNombre("Vigilante");
        rol.setPrefijo("VIG");
        rol.setDescripcion("Personal de vigilancia");
        rol.setActivo(true);

        rol = rolRepository.saveAndFlush(rol);

        vigilante = new Vigilante();
        vigilante.setCodigo("VIG00001");
        vigilante.setNombre("Juan Perez");
        vigilante.setPassword("password123");
        vigilante.setEdad(30);
        vigilante.setRol(rol);
        vigilante.setActivo(true);

        vigilante = vigilanteRepository.saveAndFlush(vigilante);
    }

    @Test
    void guardarVigilanteCorrectamente() {
        assertNotNull(vigilante.getId());
        assertEquals("Juan Perez", vigilante.getNombre());
        assertEquals(30, vigilante.getEdad());
    }

    @Test
    void buscarPorIdCorrectamente() {
        Optional<Vigilante> resultado =
                vigilanteRepository.findById(vigilante.getId());

        assertTrue(resultado.isPresent());
        assertEquals("VIG00001", resultado.get().getCodigo());
    }

    @Test
    void buscarPorIdInexistente() {
        assertTrue(vigilanteRepository.findById(99999L).isEmpty());
    }

    @Test
    void buscarPorCodigoCorrectamente() {
        Optional<Vigilante> resultado =
                vigilanteRepository.findByCodigo("VIG00001");

        assertTrue(resultado.isPresent());
        assertEquals("Juan Perez", resultado.get().getNombre());
    }

    @Test
    void buscarPorCodigoInexistente() {
        assertTrue(vigilanteRepository.findByCodigo("VIG99999").isEmpty());
    }

    @Test
    void existePorCodigo() {
        assertTrue(vigilanteRepository.existsByCodigo("VIG00001"));
    }

    @Test
    void noExistePorCodigo() {
        assertFalse(vigilanteRepository.existsByCodigo("VIG99999"));
    }

    @Test
    void listarVigilantesActivos() {
        List<Vigilante> resultado =
                vigilanteRepository.findByActivo(true);

        assertEquals(1, resultado.size());
        assertEquals("VIG00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarVigilantesInactivos() {
        assertTrue(vigilanteRepository.findByActivo(false).isEmpty());
    }

    @Test
    void buscarPorNombreParcial() {
        List<Vigilante> resultado =
                vigilanteRepository.findByNombreContainingIgnoreCase("Juan");

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorNombreSinDistinguirMayusculas() {
        List<Vigilante> resultado =
                vigilanteRepository.findByNombreContainingIgnoreCase("juan perez");

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorNombreInexistente() {
        assertTrue(vigilanteRepository
                .findByNombreContainingIgnoreCase("Carlos").isEmpty());
    }

    @Test
    void actualizarVigilanteCorrectamente() {
        vigilante.setNombre("Pedro Gomez");
        vigilante.setEdad(35);

        vigilanteRepository.saveAndFlush(vigilante);

        Vigilante resultado =
                vigilanteRepository.findById(vigilante.getId()).get();

        assertEquals("Pedro Gomez", resultado.getNombre());
        assertEquals(35, resultado.getEdad());
    }

    @Test
    void desactivarVigilanteCorrectamente() {
        vigilante.setActivo(false);
        vigilanteRepository.saveAndFlush(vigilante);

        List<Vigilante> resultado =
                vigilanteRepository.findByActivo(false);

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).isActivo());
    }
}
