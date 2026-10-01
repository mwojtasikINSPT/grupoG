package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.Asalto;

import java.util.List;

public interface AsaltoRepository extends JpaRepository<Asalto, Long> {

    List<Asalto> findByActivoTrue();
}