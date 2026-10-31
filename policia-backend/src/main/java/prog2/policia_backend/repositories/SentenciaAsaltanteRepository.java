package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.SentenciaAsaltante;

// Gestiona la persistencia y consultas de las sentencias individuales.
public interface SentenciaAsaltanteRepository extends JpaRepository<SentenciaAsaltante, Long> {

    List<SentenciaAsaltante> findByCasoJudicial_Id(Long casoId);

    List<SentenciaAsaltante> findByAsaltante_Id(Long asaltanteId);

    Optional<SentenciaAsaltante> findByCasoJudicial_IdAndAsaltante_Id(Long casoId, Long asaltanteId);

    boolean existsByCasoJudicial_IdAndAsaltante_Id(Long casoId, Long asaltanteId);
}