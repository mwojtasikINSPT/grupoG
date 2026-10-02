package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.Investigador;

import java.util.List;
import java.util.Optional;

public interface InvestigadorRepository extends JpaRepository<Investigador, Long> {

    List<Investigador> findByActivoTrue();
    Optional<Investigador> findByCodigo(String codigo);
}