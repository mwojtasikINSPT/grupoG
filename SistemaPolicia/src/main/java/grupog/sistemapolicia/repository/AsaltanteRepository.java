
package grupog.sistemapolicia.repository;

import grupog.sistemapolicia.model.Asaltante;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsaltanteRepository
        extends JpaRepository<Asaltante, String> {

    long countByBanda_NumeroBanda(String numeroBanda);

    List<Asaltante> findByBanda_NumeroBanda(
            String numeroBanda
    );
}
