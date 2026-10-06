package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import prog2.policia_backend.models.Rol;


@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    Optional<Rol> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
    
    List<Rol> findByActivo(Boolean activo);
}
