package prog2.policia_backend.repositories;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.ContratoVigilancia;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    @Modifying
    @Transactional
    @Query("UPDATE ContratoVigilancia c SET c.activo = false WHERE c.fecha < :hoy AND c.activo = true")
    int desactivarContratosVencidos(@Param("hoy") LocalDate hoy);

    @Query("""
    SELECT c FROM ContratoVigilancia c
    WHERE LOWER(c.vigilante.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))
    AND (:activo IS NULL OR c.activo = :activo)
    """)
    List<ContratoVigilancia> buscarPorNombreVigilante(
            @Param("nombre") String nombre,
            @Param("activo") Boolean activo
    );
}
