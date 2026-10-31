
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
import prog2.policia_backend.models.EntidadBancaria;
import prog2.policia_backend.models.Sucursal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:asaltorepositorytest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AsaltoRepositoryTest {

    @Autowired
    private AsaltoRepository asaltoRepository;

    @Autowired
    private AsaltanteRepository asaltanteRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private EntidadBancariaRepository entidadBancariaRepository;

    @Autowired
    private EntityManager entityManager;

    private Asalto asalto;
    private Asaltante asaltante;
    private Sucursal sucursal;

    @BeforeEach
    void setUp() {
        EntidadBancaria entidad = new EntidadBancaria();
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
    }

    private Asalto recargarAsalto() {
        entityManager.flush();
        entityManager.clear();

        return asaltoRepository.findById(asalto.getId()).get();
    }

    @Test
    void guardarAsaltoCorrectamente() {
        assertNotNull(asalto.getId());
        assertEquals("AST00001", asalto.getCodigo());
        assertEquals(LocalDate.of(2026, 10, 1), asalto.getFecha());
    }

    @Test
    void buscarPorIdCorrectamente() {
        Optional<Asalto> resultado = asaltoRepository.findById(asalto.getId());

        assertTrue(resultado.isPresent());
        assertEquals("AST00001", resultado.get().getCodigo());
    }

    @Test
    void buscarPorIdInexistente() {
        assertTrue(asaltoRepository.findById(99999L).isEmpty());
    }

    @Test
    void buscarPorCodigoCorrectamente() {
        Optional<Asalto> resultado =
                asaltoRepository.findByCodigo("AST00001");

        assertTrue(resultado.isPresent());
        assertEquals(asalto.getId(), resultado.get().getId());
    }

    @Test
    void buscarPorCodigoInexistente() {
        assertTrue(asaltoRepository.findByCodigo("AST99999").isEmpty());
    }

    @Test
    void listarTodosLosAsaltos() {
        List<Asalto> resultado = asaltoRepository.findAll();

        assertEquals(1, resultado.size());
        assertEquals("AST00001", resultado.get(0).getCodigo());
    }

    @Test
    void verificarAsaltanteParticipante() {
        assertTrue(asaltoRepository.existsByIdAndAsaltantes_Id(
                asalto.getId(), asaltante.getId()));
    }

    @Test
    void verificarAsaltanteNoParticipante() {
        assertFalse(asaltoRepository.existsByIdAndAsaltantes_Id(
                asalto.getId(), 99999L));
    }

    @Test
    void verificarAsaltoInexistente() {
        assertFalse(asaltoRepository.existsByIdAndAsaltantes_Id(
                99999L, asaltante.getId()));
    }

    @Test
    void listarAsaltosPorAsaltante() {
        List<Asalto> resultado =
                asaltoRepository.findByAsaltantes_Id(asaltante.getId());

        assertEquals(1, resultado.size());
        assertEquals("AST00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarAsaltosPorAsaltanteInexistente() {
        assertTrue(asaltoRepository.findByAsaltantes_Id(99999L).isEmpty());
    }

    @Test
    void listarAsaltosPorSucursal() {
        List<Asalto> resultado =
                asaltoRepository.findBySucursal_Id(sucursal.getId());

        assertEquals(1, resultado.size());
        assertEquals("AST00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarAsaltosPorSucursalInexistente() {
        assertTrue(asaltoRepository.findBySucursal_Id(99999L).isEmpty());
    }

    @Test
    void cambiarSucursalCorrectamente() {
        Sucursal nuevaSucursal = new Sucursal();
        nuevaSucursal.setCodigo("SUC00002");
        nuevaSucursal.setDomicilio("Avenida Santa Fe 2000");
        nuevaSucursal.setCantEmpleados(15);
        nuevaSucursal.setEntidadBancaria(sucursal.getEntidadBancaria());
        nuevaSucursal.setActivo(true);
        nuevaSucursal = sucursalRepository.saveAndFlush(nuevaSucursal);

        Long nuevaSucursalId = nuevaSucursal.getId();

        asalto.cambiarSucursal(nuevaSucursal);
        asaltoRepository.saveAndFlush(asalto);

        Asalto resultado = recargarAsalto();

        assertEquals(nuevaSucursalId, resultado.getSucursal().getId());
        assertEquals(1,
                asaltoRepository.findBySucursal_Id(nuevaSucursalId).size());
        assertTrue(
                asaltoRepository.findBySucursal_Id(sucursal.getId()).isEmpty());
    }

    @Test
    void agregarYQuitarAsaltanteCorrectamente() {
        Asaltante segundo = new Asaltante();
        segundo.setCodigo("ASS00002");
        segundo.setNombre("Pedro Gomez");
        segundo.setActivo(true);
        segundo = asaltanteRepository.saveAndFlush(segundo);

        Long segundoId = segundo.getId();

        asalto.agregarAsaltante(segundo);
        asaltoRepository.saveAndFlush(asalto);

        assertTrue(asaltoRepository.existsByIdAndAsaltantes_Id(
                asalto.getId(), segundoId));

        asalto.quitarAsaltante(segundo);
        asaltoRepository.saveAndFlush(asalto);

        entityManager.flush();
        entityManager.clear();

        assertFalse(asaltoRepository.existsByIdAndAsaltantes_Id(
                asalto.getId(), segundoId));
    }
}
