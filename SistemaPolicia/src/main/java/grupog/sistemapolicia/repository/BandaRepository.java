package grupog.sistemapolicia.repository;

import grupog.sistemapolicia.model.Banda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BandaRepository
        extends JpaRepository<Banda, String> {
}