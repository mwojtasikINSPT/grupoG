
package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import prog2.policia_backend.models.EntidadBancaria;
import prog2.policia_backend.models.Sucursal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:sucursaltest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SucursalRepositoryTest {

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private EntidadBancariaRepository entidadBancariaRepository;

    private EntidadBancaria entidad;
    private Sucursal sucursal;

    @BeforeEach
    void setUp() {
        entidad = new EntidadBancaria();
        entidad.setCodigo("EB00001");
        entidad.setNombre("Banco Nacional");
        entidad.setDomicilioCentral("Buenos Aires");
        entidad.setActivo(true);
        entidad = entidadBancariaRepository.saveAndFlush(entidad);

        sucursal = new Sucursal();
        sucursal.setCodigo("SUC00001");
        sucursal.setDomicilio("Avenida Corrientes 1234");
        sucursal.setCantEmpleados(20);
        sucursal.setEntidadBancaria(entidad);
        sucursal.setActivo(true);
        sucursal = sucursalRepository.saveAndFlush(sucursal);
    }

    @Test
    void guardarSucursalCorrectamente() {
        assertNotNull(sucursal.getId());
        assertEquals("SUC00001", sucursal.getCodigo());
        assertEquals(20, sucursal.getCantEmpleados());
    }

    @Test
    void buscarPorIdCorrectamente() {
        Optional<Sucursal> resultado =
                sucursalRepository.findById(sucursal.getId());

        assertTrue(resultado.isPresent());
        assertEquals("SUC00001", resultado.get().getCodigo());
    }

    @Test
    void buscarPorIdInexistente() {
        assertTrue(sucursalRepository.findById(99999L).isEmpty());
    }

    @Test
    void buscarPorCodigoCorrectamente() {
        Optional<Sucursal> resultado =
                sucursalRepository.findByCodigo("SUC00001");

        assertTrue(resultado.isPresent());
        assertEquals("Avenida Corrientes 1234",
                resultado.get().getDomicilio());
    }

    @Test
    void buscarPorCodigoInexistente() {
        assertTrue(sucursalRepository.findByCodigo("SUC99999").isEmpty());
    }

    @Test
    void listarSucursalesActivas() {
        List<Sucursal> resultado = sucursalRepository.findByActivo(true);

        assertEquals(1, resultado.size());
        assertEquals("SUC00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarSucursalesInactivas() {
        assertTrue(sucursalRepository.findByActivo(false).isEmpty());
    }

    @Test
    void verificarEntidadConSucursalActiva() {
        assertTrue(sucursalRepository
                .existsByEntidadBancaria_IdAndActivoTrue(entidad.getId()));
    }

    @Test
    void verificarEntidadSinSucursalActiva() {
        sucursal.setActivo(false);
        sucursalRepository.saveAndFlush(sucursal);

        assertFalse(sucursalRepository
                .existsByEntidadBancaria_IdAndActivoTrue(entidad.getId()));
    }

    @Test
    void listarSucursalesPorEntidadBancaria() {
        List<Sucursal> resultado =
                sucursalRepository.findByEntidadBancaria_Id(entidad.getId());

        assertEquals(1, resultado.size());
        assertEquals("SUC00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarSucursalesDeEntidadInexistente() {
        List<Sucursal> resultado =
                sucursalRepository.findByEntidadBancaria_Id(99999L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorDomicilioParcial() {
        List<Sucursal> resultado =
                sucursalRepository.findByDomicilioContainingIgnoreCase(
                        "Corrientes");

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorDomicilioSinDistinguirMayusculas() {
        List<Sucursal> resultado =
                sucursalRepository.findByDomicilioContainingIgnoreCase(
                        "avenida corrientes");

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorDomicilioInexistente() {
        List<Sucursal> resultado =
                sucursalRepository.findByDomicilioContainingIgnoreCase(
                        "Calle Falsa");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarDomicilioFiltrandoActivas() {
        List<Sucursal> resultado =
                sucursalRepository
                        .findByDomicilioContainingIgnoreCaseAndActivo(
                                "corrientes", true);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarDomicilioFiltrandoInactivas() {
        List<Sucursal> resultado =
                sucursalRepository
                        .findByDomicilioContainingIgnoreCaseAndActivo(
                                "corrientes", false);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void actualizarSucursalCorrectamente() {
        sucursal.setDomicilio("Avenida Santa Fe 2000");
        sucursal.setCantEmpleados(30);

        sucursalRepository.saveAndFlush(sucursal);

        Sucursal resultado =
                sucursalRepository.findById(sucursal.getId()).get();

        assertEquals("Avenida Santa Fe 2000", resultado.getDomicilio());
        assertEquals(30, resultado.getCantEmpleados());
    }

    @Test
    void desactivarSucursalCorrectamente() {
        sucursal.setActivo(false);
        sucursalRepository.saveAndFlush(sucursal);

        List<Sucursal> resultado =
                sucursalRepository.findByActivo(false);

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).isActivo());
    }
}
