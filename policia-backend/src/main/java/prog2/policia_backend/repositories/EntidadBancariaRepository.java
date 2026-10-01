package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.EntidadBancaria;

import java.util.List;

public interface EntidadBancariaRepository extends JpaRepository<EntidadBancaria, Long> {

    List<EntidadBancaria> findByActivoTrue();
}