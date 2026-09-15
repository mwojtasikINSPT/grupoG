
package grupog.sistemapolicia.repository;

import grupog.sistemapolicia.model.EntidadBancaria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntidadBancariaRepository
        extends JpaRepository<EntidadBancaria, String> { //este repo m guarda,busca,lista,elimina
}