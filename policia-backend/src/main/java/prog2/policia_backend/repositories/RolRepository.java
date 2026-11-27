package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import prog2.policia_backend.models.Rol;


@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    Optional<Rol> findByCodigo(String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Rol> findConBloqueoByCodigo(String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Rol r join r.usuarios u where u.codigo = :codigo and u.activo = true and r.activo = true and r.habilitadoParaDefensa = true")
    Optional<Rol> findConBloqueoParaDefensaByUsuarioCodigo(@Param("codigo") String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Rol r join r.usuarios u where u.codigo = :codigo and u.activo = true and r.activo = true and r.habilitadoParaVigilancia = true")
    Optional<Rol> findConBloqueoParaVigilanciaByUsuarioCodigo(@Param("codigo") String codigo);

    boolean existsByCodigo(String codigo);
    
    List<Rol> findByActivo(Boolean activo);
}
