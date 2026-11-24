package prog2.policia_backend.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import prog2.policia_backend.DTOs.PruebaDefensaDTO;
import org.springframework.data.repository.Repository;
import prog2.policia_backend.models.PruebaDefensa;

/** Expone solo insercion y consulta; no ofrece operaciones de borrado. */
public interface PruebaDefensaRepository extends Repository<PruebaDefensa, Long> {
    PruebaDefensa save(PruebaDefensa prueba);

    Optional<PruebaDefensa> findById(Long id);

    // Proyeccion explicita: nunca selecciona la columna contenido (MEDIUMBLOB).
    @Query("""
            select new prog2.policia_backend.DTOs.PruebaDefensaDTO(
                p.id, p.descripcion, p.fechaPresentacion, p.abogado.codigo,
                p.asaltante.codigo, p.casoJudicial.codigo,
                a.nombreOriginal, a.tipoContenido, a.tamano)
            from PruebaDefensa p
            left join ArchivoPruebaDefensa a on a.prueba.id = p.id
            where p.asaltante.id = :asaltanteId and p.casoJudicial.id = :casoId
            order by p.fechaPresentacion asc, p.id asc
            """)
    List<PruebaDefensaDTO> consultarMetadatos(@Param("asaltanteId") Long asaltanteId, @Param("casoId") Long casoId);
}
