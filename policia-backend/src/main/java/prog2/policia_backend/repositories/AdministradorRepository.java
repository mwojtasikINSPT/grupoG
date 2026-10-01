package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.Administrador;

import java.util.List;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    List<Administrador> findByActivoTrue();
}