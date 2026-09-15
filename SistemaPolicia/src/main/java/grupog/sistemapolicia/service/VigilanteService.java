package grupog.sistemapolicia.service;

import grupog.sistemapolicia.model.Vigilante;
import grupog.sistemapolicia.repository.VigilanteRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class VigilanteService {

    private final VigilanteRepository repository;

    public VigilanteService(
            VigilanteRepository repository) {

        this.repository = repository;
    }

    public List<Vigilante> listar() {
        return repository.findAll();
    }

    public Vigilante guardar(Vigilante vigilante) {

        validarVigilante(vigilante);

        if (repository.existsById(vigilante.getCodigo())) {
            throw new IllegalArgumentException(
                    "El código de vigilante '"
                    + vigilante.getCodigo()
                    + "' ya está registrado"
            );
        }

        return repository.save(vigilante);
    }

    private void validarVigilante(Vigilante vigilante) {

        if (vigilante == null) {
            throw new IllegalArgumentException(
                    "Los datos del vigilante son obligatorios"
            );
        }

        if (vigilante.getCodigo() == null
                || vigilante.getCodigo().isBlank()) {

            throw new IllegalArgumentException(
                    "El código del vigilante es obligatorio"
            );
        }

        if (vigilante.getEdad() < 18) {
            throw new IllegalArgumentException(
                    "El vigilante debe tener al menos 18 años"
            );
        }
    }
}