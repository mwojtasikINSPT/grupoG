package prog2.policia_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import prog2.policia_backend.models.ContratoVigilancia;

import java.util.List;

public interface ContratoVigilanciaRepository extends JpaRepository<ContratoVigilancia, Long> {

    List<ContratoVigilancia> findByActivoTrue();
}