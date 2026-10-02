package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.EntidadBancaria;

import java.util.List;
import java.util.Optional;

public interface EntidadBancariaRepository extends JpaRepository<EntidadBancaria, Long> {

    List<EntidadBancaria> findByActivo(boolean activo);
    
    Optional<EntidadBancaria> findByCodigo(String codigo);
}