package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Usuario> findByActivo(Boolean activo);

    List<Usuario> findByNombreContainingIgnoreCase(String nombre);
}
