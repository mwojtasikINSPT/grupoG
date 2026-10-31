package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import prog2.policia_backend.models.Rol;
import prog2.policia_backend.models.Usuario;
import prog2.policia_backend.models.Vigilante;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:usuariotest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Vigilante vigilante;

    @BeforeEach
    void setUp() {
        Rol rol = new Rol();
        rol.setCodigo("ROL00001");
        rol.setNombre("Vigilante");
        rol.setPrefijo("VIG");
        rol.setDescripcion("Rol de vigilancia");
        rol.setActivo(true);

        rol = rolRepository.saveAndFlush(rol);

        vigilante = new Vigilante();
        vigilante.setCodigo("VIG00001");
        vigilante.setNombre("Juan Perez");
        vigilante.setPassword("password123");
        vigilante.setEdad(30);
        vigilante.setRol(rol);
        vigilante.setActivo(true);

        vigilante = usuarioRepository.saveAndFlush(vigilante);
    }

    @Test
    void guardarUsuarioCorrectamente() {
        assertNotNull(vigilante.getId());
        assertEquals("Juan Perez", vigilante.getNombre());
    }

    @Test
    void buscarPorIdCorrectamente() {
        Optional<Usuario> resultado
                = usuarioRepository.findById(vigilante.getId());

        assertTrue(resultado.isPresent());
        assertEquals("Juan Perez", resultado.get().getNombre());
    }

    @Test
    void buscarPorIdInexistente() {
        Optional<Usuario> resultado
                = usuarioRepository.findById(99999L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorCodigoCorrectamente() {
        Optional<Usuario> resultado
                = usuarioRepository.findByCodigo("VIG00001");

        assertTrue(resultado.isPresent());
        assertEquals("Juan Perez", resultado.get().getNombre());
    }

    @Test
    void buscarPorCodigoInexistente() {
        Optional<Usuario> resultado
                = usuarioRepository.findByCodigo("VIG99999");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void existePorCodigo() {
        assertTrue(usuarioRepository.existsByCodigo("VIG00001"));
    }

    @Test
    void noExistePorCodigo() {
        assertFalse(usuarioRepository.existsByCodigo("VIG99999"));
    }

    @Test
    void listarUsuariosActivos() {
        List<Usuario> resultado
                = usuarioRepository.findByActivo(true);

        assertEquals(1, resultado.size());
        assertEquals("VIG00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarUsuariosInactivos() {
        List<Usuario> resultado
                = usuarioRepository.findByActivo(false);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorNombreParcial() {
        List<Usuario> resultado
                = usuarioRepository.findByNombreContainingIgnoreCase("Juan");

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorNombreSinDistinguirMayusculas() {
        List<Usuario> resultado
                = usuarioRepository.findByNombreContainingIgnoreCase("juan perez");

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorNombreInexistente() {
        List<Usuario> resultado
                = usuarioRepository.findByNombreContainingIgnoreCase("Carlos");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void actualizarUsuarioCorrectamente() {
        vigilante.setNombre("Pedro Gomez");
        usuarioRepository.saveAndFlush(vigilante);

        Usuario resultado
                = usuarioRepository.findById(vigilante.getId()).get();

        assertEquals("Pedro Gomez", resultado.getNombre());
    }

    @Test
    void desactivarUsuarioCorrectamente() {
        vigilante.setActivo(false);
        usuarioRepository.saveAndFlush(vigilante);

        List<Usuario> resultado
                = usuarioRepository.findByActivo(false);

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).isActivo());
    }

    @Test
    void eliminarUsuarioCorrectamente() {
        Long id = vigilante.getId();

        usuarioRepository.delete(vigilante);
        usuarioRepository.flush();

        assertFalse(usuarioRepository.findById(id).isPresent());
    }

    @Test
    void buscarPorNombreYEstadoActivo() {
        List<Usuario> resultado = usuarioRepository.findByNombreContainingIgnoreCaseAndActivo("juan", true);

        assertEquals(1, resultado.size());
        assertEquals("VIG00001", resultado.get(0).getCodigo());
    }

    @Test
    void buscarPorNombreYEstadoInactivo() {
        List<Usuario> resultado = usuarioRepository.findByNombreContainingIgnoreCaseAndActivo("juan", false);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorNombreYEstadoDespuesDeDesactivar() {
        vigilante.setActivo(false);
        usuarioRepository.saveAndFlush(vigilante);

        List<Usuario> resultado = usuarioRepository.findByNombreContainingIgnoreCaseAndActivo("JUAN", false);

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).isActivo());
    }
}
