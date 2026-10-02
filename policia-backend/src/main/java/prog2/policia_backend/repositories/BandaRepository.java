package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.Banda;

import java.util.List;
import java.util.Optional;

public interface BandaRepository extends JpaRepository<Banda, Long> {

    List<Banda> findByActivo(boolean activo);
    
    Optional<Banda> findByCodigo(String codigo);
}