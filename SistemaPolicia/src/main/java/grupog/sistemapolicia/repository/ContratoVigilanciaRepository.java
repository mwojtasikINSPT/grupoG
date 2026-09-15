
package grupog.sistemapolicia.repository;

import grupog.sistemapolicia.model.ContratoVigilancia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoVigilanciaRepository
        extends JpaRepository<ContratoVigilancia, String> {

    List<ContratoVigilancia> findBySucursal_Codigo(
            String codigoSucursal
    );

    List<ContratoVigilancia> findByVigilante_Codigo(
            String codigoVigilante
    );
}