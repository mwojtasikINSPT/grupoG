package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.Juez;

import java.util.List;

public interface JuezRepository extends JpaRepository<Juez, Long> {

    List<Juez> findByActivoTrue();
}