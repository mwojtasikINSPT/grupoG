package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.CasoJudicial;

import java.util.List;

public interface CasoJudicialRepository extends JpaRepository<CasoJudicial, Long> {

    List<CasoJudicial> findByActivoTrue();
}