package grupog.sistemapolicia;

import grupog.sistemapolicia.model.Asaltante;
import grupog.sistemapolicia.model.Asalto;
import grupog.sistemapolicia.model.Banda;
import grupog.sistemapolicia.model.CasoJudicial;
import grupog.sistemapolicia.model.EntidadBancaria;
import grupog.sistemapolicia.model.Juez;
import grupog.sistemapolicia.model.Sucursal;
import grupog.sistemapolicia.service.CasoJudicialService;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

// Se usa la base local, pero todos los registros temporales se revierten
// al terminar cada prueba. validate impide cambios en la estructura de tablas.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "spring.jpa.hibernate.ddl-auto=validate")
@Transactional
class CasoJudicialPersistenciaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CasoJudicialService service;

    @Test
    void guardaCasoConLaMismaClaveDelAsaltoYPermiteRecargarlo() {
        Datos datos = crearDatosTemporales();
        CasoJudicial caso = new CasoJudicial(datos.asalto(), datos.juez(), true, 12);
        String identificador = datos.asalto().getIdAsalto();
        assertTrue(caso.isNew());

        service.guardar(caso);
        entityManager.flush();
        assertFalse(caso.isNew());
        entityManager.clear();

        CasoJudicial guardado = entityManager.find(CasoJudicial.class, identificador);
        assertNotNull(guardado);
        assertFalse(guardado.isNew());
        assertEquals(identificador, guardado.getIdAsalto());
        assertEquals(identificador, guardado.getAsalto().getIdAsalto());
        assertTrue(guardado.isCondenado());
        assertEquals(12, guardado.getMesesCarcel());
        assertTrue(service.listarDetenidos().stream()
                .anyMatch(valor -> identificador.equals(valor.getIdAsalto())));
        assertThrows(IllegalArgumentException.class, () -> service.guardar(
                new CasoJudicial(datos.asalto(), datos.juez(), true, 6)));
    }

    @Test
    void guardaCasoSinCondenaConCeroMeses() {
        Datos datos = crearDatosTemporales();
        service.guardar(new CasoJudicial(datos.asalto(), datos.juez(), false, 12));
        entityManager.flush();
        entityManager.clear();

        CasoJudicial guardado = entityManager.find(
                CasoJudicial.class, datos.asalto().getIdAsalto());
        assertNotNull(guardado);
        assertFalse(guardado.isCondenado());
        assertEquals(0, guardado.getMesesCarcel());
    }

    private Datos crearDatosTemporales() {
        String clave = "TEST-" + UUID.randomUUID().toString().substring(0, 8);
        Banda banda = new Banda(clave, 1);
        EntidadBancaria entidad = new EntidadBancaria(clave, "Prueba temporal");
        Sucursal sucursal = new Sucursal(clave, "Prueba temporal", 1, entidad);
        Asaltante asaltante = new Asaltante(clave, "Persona ficticia", banda);
        Asalto asalto = new Asalto(clave, asaltante, sucursal, LocalDate.of(2026, 9, 15));
        Juez juez = new Juez(clave, 1, "Juez ficticio");

        entityManager.persist(banda);
        entityManager.persist(entidad);
        entityManager.persist(sucursal);
        entityManager.persist(asaltante);
        entityManager.persist(asalto);
        entityManager.persist(juez);
        entityManager.flush();
        return new Datos(asalto, juez);
    }

    private record Datos(Asalto asalto, Juez juez) {
    }
}
