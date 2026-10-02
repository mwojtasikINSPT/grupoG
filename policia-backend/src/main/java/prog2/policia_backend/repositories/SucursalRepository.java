package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import prog2.policia_backend.models.Sucursal;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {

    List<Sucursal> findByActivo(boolean activo);

    // Comprueba si la entidad bancaria tiene al menos una sucursal activa.
    boolean existsByEntidadBancaria_IdAndActivoTrue(Long entidadBancariaId);

    Optional<Sucursal> findByCodigo(String codigo);
}
