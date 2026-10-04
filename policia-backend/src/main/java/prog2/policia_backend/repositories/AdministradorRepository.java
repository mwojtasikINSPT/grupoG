package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.Administrador;

import java.util.List;
import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    List<Administrador> findByActivo(boolean activo);
    Optional<Administrador> findByCodigo(String codigo);
    List<Administrador> findByNombreContainingIgnoreCase(String nombre);
}