package grupog.sistemapolicia.repository;

import grupog.sistemapolicia.model.CasoJudicial;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CasoJudicialRepository
        extends JpaRepository<CasoJudicial, String> {

    List<CasoJudicial> findByCondenadoTrue();
}
