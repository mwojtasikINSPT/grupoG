
package prog2.policia_backend.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import jakarta.persistence.EntityManager;

import prog2.policia_backend.models.ContratoVigilancia;
import prog2.policia_backend.models.EntidadBancaria;
import prog2.policia_backend.models.Rol;
import prog2.policia_backend.models.Sucursal;
import prog2.policia_backend.models.Vigilante;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:contratotest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ContratoVigilanciaRepositoryTest {

    @Autowired
    private ContratoVigilanciaRepository contratoRepository;

    @Autowired
    private VigilanteRepository vigilanteRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private EntidadBancariaRepository entidadBancariaRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private EntityManager entityManager;

    private Vigilante vigilante;
    private Sucursal sucursal;
    private ContratoVigilancia contrato;

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

        EntidadBancaria entidad = new EntidadBancaria();
        entidad.setCodigo("BAN00001");
        entidad.setNombre("Banco Central");
        entidad.setDomicilioCentral("Buenos Aires");
        entidad.setActivo(true);
        entidad = entidadBancariaRepository.saveAndFlush(entidad);

        sucursal = new Sucursal();
        sucursal.setCodigo("SUC00001");
        sucursal.setDomicilio("Calle Principal 123");
        sucursal.setCantEmpleados(20);
        sucursal.setEntidadBancaria(entidad);
        sucursal.setActivo(true);
        sucursal = sucursalRepository.saveAndFlush(sucursal);

        contrato = crearContrato(
                "CON00001",
                LocalDate.of(2026, 11, 15),
                true
        );
    }

    private ContratoVigilancia crearContrato(
            String codigo, LocalDate fecha, boolean activo) {

        ContratoVigilancia nuevo = new ContratoVigilancia();
        nuevo.setCodigo(codigo);
        nuevo.setFecha(fecha);
        nuevo.setConArma(true);
        nuevo.setVigilante(vigilante);
        nuevo.setSucursal(sucursal);
        nuevo.setActivo(activo);

        return contratoRepository.saveAndFlush(nuevo);
    }

    @Test
    void guardarContratoCorrectamente() {
        assertNotNull(contrato.getId());
        assertEquals("CON00001", contrato.getCodigo());
        assertTrue(contrato.getActivo());
    }

    @Test
    void buscarPorIdCorrectamente() {
        Optional<ContratoVigilancia> resultado =
                contratoRepository.findById(contrato.getId());

        assertTrue(resultado.isPresent());
        assertEquals("CON00001", resultado.get().getCodigo());
    }

    @Test
    void buscarPorIdInexistente() {
        assertTrue(contratoRepository.findById(99999L).isEmpty());
    }

    @Test
    void buscarPorCodigoCorrectamente() {
        Optional<ContratoVigilancia> resultado =
                contratoRepository.findByCodigo("CON00001");

        assertTrue(resultado.isPresent());
        assertEquals(contrato.getId(), resultado.get().getId());
    }

    @Test
    void buscarPorCodigoInexistente() {
        assertTrue(contratoRepository.findByCodigo("CON99999").isEmpty());
    }

    @Test
    void listarContratosActivos() {
        List<ContratoVigilancia> resultado =
                contratoRepository.findByActivo(true);

        assertEquals(1, resultado.size());
        assertEquals("CON00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarContratosInactivos() {
        assertTrue(contratoRepository.findByActivo(false).isEmpty());
    }

    @Test
    void verificarContratoActivoEnMismaFecha() {
        boolean existe =
                contratoRepository.existsByVigilante_IdAndFechaAndActivoTrue(
                        vigilante.getId(),
                        contrato.getFecha()
                );

        assertTrue(existe);
    }

    @Test
    void verificarContratoEnFechaDiferente() {
        boolean existe =
                contratoRepository.existsByVigilante_IdAndFechaAndActivoTrue(
                        vigilante.getId(),
                        LocalDate.of(2026, 12, 20)
                );

        assertFalse(existe);
    }

    @Test
    void verificarContratoExcluyendoSuPropioId() {
        boolean existe =
                contratoRepository.existsByVigilante_IdAndFechaAndActivoTrueAndIdNot(
                        vigilante.getId(),
                        contrato.getFecha(),
                        contrato.getId()
                );

        assertFalse(existe);
    }

    @Test
    void verificarContratoExcluyendoOtroId() {
        boolean existe =
                contratoRepository.existsByVigilante_IdAndFechaAndActivoTrueAndIdNot(
                        vigilante.getId(),
                        contrato.getFecha(),
                        99999L
                );

        assertTrue(existe);
    }

    @Test
    void verificarContratoActivoFuturo() {
        boolean existe =
                contratoRepository.existsByVigilante_IdAndFechaGreaterThanEqualAndActivoTrue(
                        vigilante.getId(),
                        LocalDate.of(2026, 11, 1)
                );

        assertTrue(existe);
    }

    @Test
    void verificarContratoSinFechasFuturas() {
        boolean existe =
                contratoRepository.existsByVigilante_IdAndFechaGreaterThanEqualAndActivoTrue(
                        vigilante.getId(),
                        LocalDate.of(2027, 1, 1)
                );

        assertFalse(existe);
    }

    @Test
    void listarContratosPorVigilante() {
        List<ContratoVigilancia> resultado =
                contratoRepository.findByVigilante_Id(vigilante.getId());

        assertEquals(1, resultado.size());
        assertEquals("CON00001", resultado.get(0).getCodigo());
    }

    @Test
    void listarContratosPorSucursal() {
        List<ContratoVigilancia> resultado =
                contratoRepository.findBySucursal_Id(sucursal.getId());

        assertEquals(1, resultado.size());
    }

    @Test
    void listarContratosFuturosActivosPorSucursal() {
        List<ContratoVigilancia> resultado =
                contratoRepository.findBySucursal_IdAndFechaAfterAndActivoTrue(
                        sucursal.getId(),
                        LocalDate.of(2026, 11, 1)
                );

        assertEquals(1, resultado.size());
    }

    @Test
    void noListarContratosVencidosPorSucursal() {
        List<ContratoVigilancia> resultado =
                contratoRepository.findBySucursal_IdAndFechaAfterAndActivoTrue(
                        sucursal.getId(),
                        LocalDate.of(2026, 12, 1)
                );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void actualizarContratoCorrectamente() {
        contrato.setConArma(false);
        contratoRepository.saveAndFlush(contrato);

        ContratoVigilancia resultado =
                contratoRepository.findById(contrato.getId()).get();

        assertFalse(resultado.getConArma());
    }

    @Test
    void desactivarContratoCorrectamente() {
        contrato.setActivo(false);
        contratoRepository.saveAndFlush(contrato);

        assertEquals(1, contratoRepository.findByActivo(false).size());
        assertTrue(contratoRepository.findByActivo(true).isEmpty());
    }

    @Test
    void desactivarContratosVencidos() {
        contrato.setFecha(LocalDate.of(2026, 10, 1));
        contratoRepository.saveAndFlush(contrato);

        int modificados =
                contratoRepository.desactivarContratosVencidos(
                        LocalDate.of(2026, 10, 31)
                );

        entityManager.clear();

        ContratoVigilancia resultado =
                contratoRepository.findByCodigo("CON00001").get();

        assertEquals(1, modificados);
        assertFalse(resultado.getActivo());
    }

    @Test
    void noDesactivarContratosFuturos() {
        int modificados =
                contratoRepository.desactivarContratosVencidos(
                        LocalDate.of(2026, 10, 31)
                );

        entityManager.clear();

        ContratoVigilancia resultado =
                contratoRepository.findByCodigo("CON00001").get();

        assertEquals(0, modificados);
        assertTrue(resultado.getActivo());
    }

    @Test
    void noDesactivarContratosDelDiaActual() {
        contrato.setFecha(LocalDate.of(2026, 10, 31));
        contratoRepository.saveAndFlush(contrato);

        int modificados =
                contratoRepository.desactivarContratosVencidos(
                        LocalDate.of(2026, 10, 31)
                );

        entityManager.clear();

        ContratoVigilancia resultado =
                contratoRepository.findByCodigo("CON00001").get();

        assertEquals(0, modificados);
        assertTrue(resultado.getActivo());
    }

    @Test
    void noContarContratosInactivosComoDisponibles() {
        contrato.setActivo(false);
        contratoRepository.saveAndFlush(contrato);

        boolean existe =
                contratoRepository.existsByVigilante_IdAndFechaAndActivoTrue(
                        vigilante.getId(),
                        contrato.getFecha()
                );

        assertFalse(existe);
    }
}
