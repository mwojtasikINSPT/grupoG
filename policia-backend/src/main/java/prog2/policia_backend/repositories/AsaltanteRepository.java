package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.Asaltante;

public interface AsaltanteRepository extends JpaRepository<Asaltante, Long> {//proporciona automáticamente save(), findById(), findAll(), deleteById(), existsById().
    List<Asaltante> findByActivo(boolean activo);
    
    // Comprueba si la banda tiene al menos un asaltante activo
    boolean existsByBanda_IdAndActivoTrue(Long bandaId);
    
    Optional<Asaltante> findByCodigo(String codigo);
}
