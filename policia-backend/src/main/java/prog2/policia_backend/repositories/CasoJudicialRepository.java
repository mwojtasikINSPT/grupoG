package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.CasoJudicial;

// Gestiona las consultas de persistencia de los casos judiciales.
public interface CasoJudicialRepository extends JpaRepository<CasoJudicial, Long> {

    List<CasoJudicial> findByActivo(boolean activo);

    boolean existsByAsalto_Id(Long asaltoId);

    boolean existsByJuez_Id(Long juezId);

    Optional<CasoJudicial> findByCodigo(String codigo);

    List<CasoJudicial> findBySentencias_Asaltante_Id(Long asaltanteId);

    List<CasoJudicial> findByJuez_Id(Long juezId);
}