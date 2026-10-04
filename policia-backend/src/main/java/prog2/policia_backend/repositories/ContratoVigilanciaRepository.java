package prog2.policia_backend.repositories;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.ContratoVigilancia;

import java.util.List;
import java.util.Optional;

public interface ContratoVigilanciaRepository extends JpaRepository<ContratoVigilancia, Long> {

    List<ContratoVigilancia> findByActivo(Boolean activo);

    boolean existsByVigilante_IdAndFechaAndActivoTrue(
            Long vigilanteId,
            LocalDate fecha
    );

    boolean existsByVigilante_IdAndFechaAndActivoTrueAndIdNot(
            Long vigilanteId,
            LocalDate fecha,
            Long id
    );

    // Comprueba si el vigilante tiene algún contrato activo con fecha hoy o futura.
    boolean existsByVigilante_IdAndFechaGreaterThanEqualAndActivoTrue(
        Long vigilanteId,
        LocalDate fecha);

    // Obtiene todos los contratos asociados a un vigilante, incluidos los inactivos.
    List<ContratoVigilancia> findByVigilante_Id(Long vigilanteId);

    // Obtiene los contratos activos de una sucursal cuya fecha todavía es futura.
    List<ContratoVigilancia> findBySucursal_IdAndFechaAfterAndActivoTrue(
            Long sucursalId,
            LocalDate fecha
    );
    
    //Búsqueda por cód
    Optional<ContratoVigilancia> findByCodigo(String codigo);
    
    List<ContratoVigilancia> findBySucursal_Id(Long sucursalId);

}
