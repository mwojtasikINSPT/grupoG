package prog2.policia_backend.repositories;

import java.util.List;
import org.springframework.data.repository.Repository;
import prog2.policia_backend.models.PruebaDefensa;

/** Expone solo insercion y consulta; no ofrece operaciones de borrado. */
public interface PruebaDefensaRepository extends Repository<PruebaDefensa, Long> {
    PruebaDefensa save(PruebaDefensa prueba);

    List<PruebaDefensa> findByAsaltante_IdAndCasoJudicial_IdOrderByFechaPresentacionAscIdAsc(Long asaltanteId, Long casoId);
}
