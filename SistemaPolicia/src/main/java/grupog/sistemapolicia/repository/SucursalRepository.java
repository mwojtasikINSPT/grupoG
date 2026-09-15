
package grupog.sistemapolicia.repository;

import grupog.sistemapolicia.model.Sucursal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SucursalRepository
        extends JpaRepository<Sucursal, String> {

    List<Sucursal> findByEntidad_Codigo(
            String codigoEntidad
    );
}