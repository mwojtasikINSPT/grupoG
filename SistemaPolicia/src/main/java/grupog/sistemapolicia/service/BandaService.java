
package grupog.sistemapolicia.service;

import grupog.sistemapolicia.model.Banda;
import grupog.sistemapolicia.repository.BandaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BandaService {

    private final BandaRepository repository;

    public BandaService(BandaRepository repository) {
        this.repository = repository;
    }

    public List<Banda> listar() {
        return repository.findAll();
    }

    public Banda guardar(Banda banda) {

        validarBanda(banda);

        if (repository.existsById(
                banda.getNumeroBanda())) {

            throw new IllegalArgumentException(
                    "La banda número '"
                    + banda.getNumeroBanda()
                    + "' ya está registrada"
            );
        }

        return repository.save(banda);
    }

    private void validarBanda(Banda banda) {

        if (banda == null) {
            throw new IllegalArgumentException(
                    "Los datos de la banda son obligatorios"
            );
        }

        if (banda.getNumeroBanda() == null
                || banda.getNumeroBanda().isBlank()) {

            throw new IllegalArgumentException(
                    "El número de banda es obligatorio"
            );
        }

        if (banda.getCantMiembros() < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de miembros no puede ser negativa"
            );
        }
    }
}