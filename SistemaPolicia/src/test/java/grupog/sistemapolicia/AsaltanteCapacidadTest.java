package grupog.sistemapolicia;

import grupog.sistemapolicia.model.Asaltante;
import grupog.sistemapolicia.model.Banda;
import grupog.sistemapolicia.repository.AsaltanteRepository;
import grupog.sistemapolicia.service.AsaltanteService;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

// Los datos temporales se revierten y no se modifica la estructura de tablas.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "spring.jpa.hibernate.ddl-auto=validate")
@Transactional
class AsaltanteCapacidadTest {

    @Autowired private EntityManager entityManager;
    @Autowired private AsaltanteService service;
    @Autowired private AsaltanteRepository repository;

    @Test
    void permiteDosIntegrantesPeroRechazaElTercero() {
        Banda banda = crearBanda(2);
        service.guardar(new Asaltante(clave(), "Persona ficticia 1", banda));
        service.guardar(new Asaltante(clave(), "Persona ficticia 2", banda));
        entityManager.flush();
        String terceraClave = clave();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.guardar(new Asaltante(terceraClave, "Persona ficticia 3", banda)));

        assertTrue(error.getMessage().contains("ya está completa"));
        assertEquals(2, repository.countByBanda_NumeroBanda(banda.getNumeroBanda()));
        assertFalse(repository.existsById(terceraClave));
    }

    @Test
    void rechazaIntegrantesEnUnaBandaDeCeroPersonas() {
        Banda banda = crearBanda(0);
        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(new Asaltante(clave(), "Persona ficticia", banda)));
        assertEquals(0, repository.countByBanda_NumeroBanda(banda.getNumeroBanda()));
    }

    @Test
    void noBorraLosIntegrantesPreviosQueYaSuperabanElLimite() {
        Banda banda = crearBanda(2);
        for (int i = 0; i < 3; i++) {
            entityManager.persist(new Asaltante(clave(), "Persona ficticia", banda));
        }
        entityManager.flush();
        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(new Asaltante(clave(), "Otro integrante", banda)));
        assertEquals(3, repository.countByBanda_NumeroBanda(banda.getNumeroBanda()));
    }

    private Banda crearBanda(int cantidad) {
        Banda banda = new Banda(clave(), cantidad);
        entityManager.persist(banda);
        entityManager.flush();
        return banda;
    }

    private String clave() {
        return "TEST-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
