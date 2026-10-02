package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.Sucursal;

import java.util.List;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {

    List<Sucursal> findByActivoTrue();

    // Comprueba si la entidad bancaria tiene al menos una sucursal activa.
    boolean existsByEntidadBancaria_IdAndActivoTrue(Long entidadBancariaId);
}
