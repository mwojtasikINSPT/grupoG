package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import prog2.policia_backend.models.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Carga el rol y sus permisos durante la autenticación.
    @EntityGraph(attributePaths = {"rol", "rol.permisosPorEntidad"})
    Optional<Usuario> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Usuario> findByRol_NombreAndActivoTrue(String nombreRol);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<Usuario> findConBloqueoByCodigo(String codigo);

    List<Usuario> findByActivo(Boolean activo);
    boolean existsByRol_IdAndActivoTrue(Long rolId);
}
