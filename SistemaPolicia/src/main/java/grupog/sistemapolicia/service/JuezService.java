package grupog.sistemapolicia.service;

import grupog.sistemapolicia.model.Juez;
import grupog.sistemapolicia.repository.JuezRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JuezService {

    private final JuezRepository repository;

    public JuezService(JuezRepository repository) {
        this.repository = repository;
    }

    public List<Juez> listar() {
        return repository.findAll();
    }

    public Juez guardar(Juez juez) {
        if (juez == null) {
            throw new IllegalArgumentException(
                    "Los datos del juez son obligatorios");
        }
        if (juez.getClaveInterna() == null
                || juez.getClaveInterna().isBlank()) {
            throw new IllegalArgumentException(
                    "La clave interna del juez es obligatoria");
        }
        if (juez.getNombre() == null || juez.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del juez es obligatorio");
        }
        if (juez.getAniosServicio() < 0) {
            throw new IllegalArgumentException(
                    "Los años de servicio no pueden ser negativos");
        }
        if (repository.existsById(juez.getClaveInterna())) {
            throw new IllegalArgumentException(
                    "La clave de juez '" + juez.getClaveInterna()
                    + "' ya está registrada");
        }
        return repository.save(juez);
    }
}
