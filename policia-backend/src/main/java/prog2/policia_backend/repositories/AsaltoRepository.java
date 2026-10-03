package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import prog2.policia_backend.models.Asalto;

public interface AsaltoRepository extends JpaRepository<Asalto, Long> {

    boolean existsByIdAndAsaltantes_Id(
            Long asaltoId,
            Long asaltanteId
    );

    Optional<Asalto> findByCodigo(String codigo);

    List<Asalto> findByAsaltantes_Id(Long asaltanteId);

}
