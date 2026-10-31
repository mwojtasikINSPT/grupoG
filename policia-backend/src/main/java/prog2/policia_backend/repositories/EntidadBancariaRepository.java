package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import prog2.policia_backend.models.EntidadBancaria;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EntidadBancariaRepository extends JpaRepository<EntidadBancaria, Long> {

    List<EntidadBancaria> findByActivo(boolean activo);

    Optional<EntidadBancaria> findByCodigo(String codigo);

    List<EntidadBancaria> findByNombreContainingIgnoreCase(String nombre);

    //List<EntidadBancaria> findByDomicilioCentralContainingIgnoreCase(String domicilio);
    @Query("""
    SELECT e FROM EntidadBancaria e
    WHERE (:nombre IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
    AND (:domicilio IS NULL OR LOWER(e.domicilioCentral) LIKE LOWER(CONCAT('%', :domicilio, '%')))
    AND (:activo IS NULL OR e.activo = :activo)
    """)
    List<EntidadBancaria> buscarConFiltros(@Param("nombre") String nombre, @Param("domicilio") String domicilio, @Param("activo") Boolean activo);
}
