
package grupog.sistemapolicia.repository;

import grupog.sistemapolicia.model.Vigilante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VigilanteRepository
        extends JpaRepository<Vigilante, String> {
}