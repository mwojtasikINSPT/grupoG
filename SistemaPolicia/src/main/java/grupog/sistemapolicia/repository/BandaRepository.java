package grupog.sistemapolicia.repository;

import grupog.sistemapolicia.model.Banda;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BandaRepository
        extends JpaRepository<Banda, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Banda b where b.numeroBanda = :numeroBanda")
    Optional<Banda> buscarParaRegistrarIntegrante(
            @Param("numeroBanda") String numeroBanda);
}
