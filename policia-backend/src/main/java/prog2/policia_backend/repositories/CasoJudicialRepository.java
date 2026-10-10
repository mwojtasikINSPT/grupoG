package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import prog2.policia_backend.models.CasoJudicial;
import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.models.Asalto;

public interface CasoJudicialRepository extends JpaRepository<CasoJudicial, Long> {

    List<CasoJudicial> findByActivo(boolean activo);

    boolean existsByAsalto_IdAndAsaltantes_Id(Long asaltoId, Long asaltanteId);

    boolean existsByAsaltoAndAsaltantesContains(Asalto asalto, Asaltante asaltante);

    // Comprueba si el juez tiene algun caso judicial asociado
    boolean existsByJuez_IdAndActivoTrue(Long juezId);

    Optional<CasoJudicial> findByCodigo(String codigo);

    List<CasoJudicial> findByAsaltantes_Id(Long asaltanteId);

    List<CasoJudicial> findByJuez_Id(Long juezId);
}
