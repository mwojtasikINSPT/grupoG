package prog2.policia_backend.repositories;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.ContratoVigilancia;

import java.util.List;

public interface ContratoVigilanciaRepository extends JpaRepository<ContratoVigilancia, Long> {

    List<ContratoVigilancia> findByActivoTrue();

    boolean existsByVigilante_IdAndFechaAndActivoTrue(
        Long vigilanteId,
        LocalDate fecha
    );
    
    boolean existsByVigilante_IdAndFechaAndActivoTrueAndIdNot(
        Long vigilanteId,
        LocalDate fecha,
        Long id
);
}
