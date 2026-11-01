package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import prog2.policia_backend.models.EntidadBancaria;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:entidadbancariatest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class EntidadBancariaRepositoryTest {

    @Autowired
    private EntidadBancariaRepository entidadBancariaRepository;

    private EntidadBancaria entidad;

    @BeforeEach
    void setUp() {
        entidad = new EntidadBancaria();
        entidad.setCodigo("EB00001");
        entidad.setNombre("Banco Nacional");
        entidad.setDomicilioCentral("Buenos Aires");
        entidad.setActivo(true);

        entidad = entidadBancariaRepository.saveAndFlush(entidad);
    }

    @Test
    void guardarEntidadCorrectamente() {
        assertNotNull(entidad.getId());
        assertEquals("EB00001", entidad.getCodigo());
        assertEquals("Banco Nacional", entidad.getNombre());
        assertTrue(entidad.isActivo());
    }

    @Test
    void buscarPorIdCorrectamente() {
        Optional<EntidadBancaria> resultado
                = entidadBancariaRepository.findById(entidad.getId());

        assertTrue(resultado.isPresent());
        assertEquals("Banco Nacional", resultado.get().getNombre());
    }

    @Test
    void buscarPorIdInexistente() {
        Optional<EntidadBancaria> resultado
                = entidadBancariaRepository.findById(99999L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorCodigoCorrectamente() {
        Optional<EntidadBancaria> resultado
                = entidadBancariaRepository.findByCodigo("EB00001");

        assertTrue(resultado.isPresent());
        assertEquals("Banco Nacional", resultado.get().getNombre());
    }

    @Test
    void buscarPorCodigoInexistente() {
        Optional<EntidadBancaria> resultado
                = entidadBancariaRepository.findByCodigo("EB99999");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void listarEntidadesActivas() {
        List<EntidadBancaria> resultado
                = entidadBancariaRepository.findByActivo(true);

        assertEquals(1, resultado.size());
        assertEquals("EB00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarEntidadesInactivas() {
        List<EntidadBancaria> resultado
                = entidadBancariaRepository.findByActivo(false);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorNombreParcial() {
        List<EntidadBancaria> resultado
                = entidadBancariaRepository.findByNombreContainingIgnoreCase("Nacional");

        assertEquals(1, resultado.size());
        assertEquals("Banco Nacional", resultado.get(0).getNombre());
    }

    @Test
    void buscarPorNombreSinDistinguirMayusculas() {
        List<EntidadBancaria> resultado
                = entidadBancariaRepository.findByNombreContainingIgnoreCase("banco nacional");

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorNombreInexistente() {
        List<EntidadBancaria> resultado
                = entidadBancariaRepository.findByNombreContainingIgnoreCase("Santander");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void actualizarNombreCorrectamente() {
        entidad.setNombre("Banco Federal");
        entidadBancariaRepository.saveAndFlush(entidad);

        EntidadBancaria resultado
                = entidadBancariaRepository.findById(entidad.getId()).get();

        assertEquals("Banco Federal", resultado.getNombre());
    }

    @Test
    void actualizarDomicilioCorrectamente() {
        entidad.setDomicilioCentral("Cordoba");
        entidadBancariaRepository.saveAndFlush(entidad);

        EntidadBancaria resultado
                = entidadBancariaRepository.findById(entidad.getId()).get();

        assertEquals("Cordoba", resultado.getDomicilioCentral());
    }

    @Test
    void desactivarEntidadCorrectamente() {
        entidad.setActivo(false);
        entidadBancariaRepository.saveAndFlush(entidad);

        List<EntidadBancaria> resultado
                = entidadBancariaRepository.findByActivo(false);

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).isActivo());
    }

    @Test
    void reactivarEntidadCorrectamente() {
        entidad.setActivo(false);
        entidadBancariaRepository.saveAndFlush(entidad);

        entidad.setActivo(true);
        entidadBancariaRepository.saveAndFlush(entidad);

        EntidadBancaria resultado
                = entidadBancariaRepository.findById(entidad.getId()).get();

        assertTrue(resultado.isActivo());
        assertEquals(1, entidadBancariaRepository.findByActivo(true).size());
    }

    @Test
    void eliminarEntidadCorrectamente() {
        Long id = entidad.getId();

        entidadBancariaRepository.delete(entidad);
        entidadBancariaRepository.flush();

        assertFalse(entidadBancariaRepository.findById(id).isPresent());
    }

    @Test
    void buscarConFiltrosPorNombre() {
        List<EntidadBancaria> resultado = entidadBancariaRepository.buscarConFiltros("nacional", null, null);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarConFiltrosPorDomicilio() {
        List<EntidadBancaria> resultado = entidadBancariaRepository.buscarConFiltros(null, "aires", null);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarConFiltrosPorEstadoActivo() {
        List<EntidadBancaria> resultado = entidadBancariaRepository.buscarConFiltros(null, null, true);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarConFiltrosPorEstadoInactivo() {
        List<EntidadBancaria> resultado = entidadBancariaRepository.buscarConFiltros(null, null, false);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarConTodosLosFiltros() {
        List<EntidadBancaria> resultado = entidadBancariaRepository.buscarConFiltros("BANCO", "BUENOS", true);

        assertEquals(1, resultado.size());
        assertEquals("EB00001", resultado.get(0).getCodigo());
    }

    @Test
    void buscarConFiltrosSinCoincidencias() {
        List<EntidadBancaria> resultado = entidadBancariaRepository.buscarConFiltros("Santander", "Cordoba", true);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarConFiltrosEntidadDesactivada() {
        entidad.setActivo(false);
        entidadBancariaRepository.saveAndFlush(entidad);

        List<EntidadBancaria> resultado = entidadBancariaRepository.buscarConFiltros("banco", "aires", false);

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).isActivo());
    }

    @Test
    void existeEntidadConNombreIgnorandoMayusculas() {
        boolean existe = entidadBancariaRepository.existsByNombreIgnoreCase("banco nacional");

        assertTrue(existe);
    }

    @Test
    void noExisteEntidadConNombreDiferente() {
        boolean existe = entidadBancariaRepository.existsByNombreIgnoreCase("Banco Provincial");

        assertFalse(existe);
    }

    @Test
    void detectarNombreDuplicadoExcluyendoEntidadActual() {
        EntidadBancaria otra = new EntidadBancaria();
        otra.setCodigo("EB00002");
        otra.setNombre("Banco Provincial");
        otra.setDomicilioCentral("Cordoba");
        otra.setActivo(true);

        otra = entidadBancariaRepository.saveAndFlush(otra);

        boolean existe = entidadBancariaRepository.existsByNombreIgnoreCaseAndIdNot("Banco Nacional", otra.getId());

        assertTrue(existe);
    }

    @Test
    void permitirConservarNombrePropio() {
        boolean existe = entidadBancariaRepository.existsByNombreIgnoreCaseAndIdNot("Banco Nacional", entidad.getId());

        assertFalse(existe);
    }
}
