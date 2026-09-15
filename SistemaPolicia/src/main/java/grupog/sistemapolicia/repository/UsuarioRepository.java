package grupog.sistemapolicia.repository;

import grupog.sistemapolicia.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
}
