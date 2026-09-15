
package grupog.sistemapolicia.service;

import grupog.sistemapolicia.model.EntidadBancaria;
import grupog.sistemapolicia.repository.EntidadBancariaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EntidadBancariaService {

    private final EntidadBancariaRepository repository;

    public EntidadBancariaService(
            EntidadBancariaRepository repository) {

        this.repository = repository;
    }

    public List<EntidadBancaria> listar() {
        return repository.findAll();
    }

    public EntidadBancaria guardar(EntidadBancaria entidad) {

        if (entidad.getCodigo() == null
                || entidad.getCodigo().isBlank()) {

            throw new IllegalArgumentException(
                    "El código es obligatorio"
            );
        }

        if (entidad.getDomicilioCentral() == null
                || entidad.getDomicilioCentral().isBlank()) {

            throw new IllegalArgumentException(
                    "El domicilio central es obligatorio"
            );
        }

        return repository.save(entidad);
    }
}