
package prog2.policia_backend.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import prog2.policia_backend.models.Rol;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:roltest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RolRepositoryTest {

    @Autowired
    private RolRepository rolRepository;

    private Rol rol;

    @BeforeEach
    void setUp() {
        rolRepository.deleteAll();

        rol = new Rol();
        rol.setCodigo("ROL00001");
        rol.setNombre("Administrador");
        rol.setPrefijo("ADM");
        rol.setDescripcion("Acceso administrativo");
        rol.setActivo(true);

        rolRepository.saveAndFlush(rol);
    }

    @Test
    void buscarPorNombreCorrectamente() {
        assertTrue(rolRepository.findByNombre("Administrador").isPresent());
    }

    @Test
    void buscarPorNombreInexistente() {
        assertTrue(rolRepository.findByNombre("Inexistente").isEmpty());
    }

    @Test
    void existePorNombre() {
        assertTrue(rolRepository.existsByNombre("Administrador"));
    }

    @Test
    void noExistePorNombre() {
        assertFalse(rolRepository.existsByNombre("Inexistente"));
    }

    @Test
    void buscarPorCodigoCorrectamente() {
        assertTrue(rolRepository.findByCodigo("ROL00001").isPresent());
    }

    @Test
    void buscarPorCodigoInexistente() {
        assertTrue(rolRepository.findByCodigo("ROL99999").isEmpty());
    }

    @Test
    void existePorCodigo() {
        assertTrue(rolRepository.existsByCodigo("ROL00001"));
    }

    @Test
    void listarRolesActivos() {
        assertEquals(1, rolRepository.findByActivo(true).size());
    }

    @Test
    void listarRolesInactivos() {
        assertTrue(rolRepository.findByActivo(false).isEmpty());
    }

    @Test
    void guardarRolCorrectamente() {
        assertNotNull(rol.getId());
        assertEquals("Administrador",
                rolRepository.findById(rol.getId()).get().getNombre());
    }
}
