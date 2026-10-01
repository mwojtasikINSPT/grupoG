package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.Vigilante;

import java.util.List;

public interface VigilanteRepository extends JpaRepository<Vigilante, Long> {

    List<Vigilante> findByActivoTrue();
}