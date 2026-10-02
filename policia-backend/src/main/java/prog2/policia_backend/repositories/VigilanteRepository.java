package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import prog2.policia_backend.models.Vigilante;

public interface VigilanteRepository extends JpaRepository<Vigilante, Long> {

    List<Vigilante> findByActivo(boolean activo);

    Optional<Vigilante> findByCodigo(String codigo);

}
