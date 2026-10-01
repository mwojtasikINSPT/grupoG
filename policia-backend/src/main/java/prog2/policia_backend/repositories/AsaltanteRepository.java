package prog2.policia_backend.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.Asaltante;

public interface AsaltanteRepository extends JpaRepository<Asaltante, Long> {

    //proporciona automáticamente save(), findById(), findAll(), deleteById(), existsById().
    List<Asaltante> findByActivoTrue();
}
