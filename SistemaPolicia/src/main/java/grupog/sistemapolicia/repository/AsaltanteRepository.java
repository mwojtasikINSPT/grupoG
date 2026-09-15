
package grupog.sistemapolicia.repository;

import grupog.sistemapolicia.model.Asaltante;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsaltanteRepository
        extends JpaRepository<Asaltante, String> {

    List<Asaltante> findByBanda_NumeroBanda(
            String numeroBanda
    );
}
