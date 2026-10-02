package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.CasoJudicial;

import java.util.List;
import java.util.Optional;

public interface CasoJudicialRepository extends JpaRepository<CasoJudicial, Long> {

    List<CasoJudicial> findByActivo(boolean activo);

    boolean existsByAsalto_IdAndAsaltante_Id(
            Long asaltoId,
            Long asaltanteId
    );
    
    // Comprueba si el juez tiene algun caso judicial asociado
    boolean existsByJuez_Id(Long juezId);
    
    Optional<CasoJudicial> findByCodigo(String codigo);
}
