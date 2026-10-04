package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.Juez;

import java.util.List;
import java.util.Optional;

public interface JuezRepository extends JpaRepository<Juez, Long> {

    List<Juez> findByActivo(boolean activo);
    
    Optional<Juez> findByCodigo(String codigo);
    
    
    List<Juez> findByNombreContainingIgnoreCase(String nombre);
}