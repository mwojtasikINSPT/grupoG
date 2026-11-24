package prog2.policia_backend.repositories;

import java.util.Optional;
import org.springframework.data.repository.Repository;
import prog2.policia_backend.models.ArchivoPruebaDefensa;

public interface ArchivoPruebaDefensaRepository extends Repository<ArchivoPruebaDefensa, Long> {
    ArchivoPruebaDefensa save(ArchivoPruebaDefensa archivo);

    Optional<ArchivoPruebaDefensa> findById(Long id);
}
