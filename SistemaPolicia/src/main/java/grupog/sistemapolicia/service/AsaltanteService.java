package grupog.sistemapolicia.service;

import grupog.sistemapolicia.model.Asaltante;
import grupog.sistemapolicia.model.Banda;
import grupog.sistemapolicia.repository.AsaltanteRepository;
import grupog.sistemapolicia.repository.BandaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsaltanteService {

    private final AsaltanteRepository asaltanteRepository;
    private final BandaRepository bandaRepository;

    public AsaltanteService(
            AsaltanteRepository asaltanteRepository,
            BandaRepository bandaRepository) {

        this.asaltanteRepository = asaltanteRepository;
        this.bandaRepository = bandaRepository;
    }

    public List<Asaltante> listar() {
        return asaltanteRepository.findAll();
    }

    @Transactional
    public Asaltante guardar(Asaltante asaltante) {

        validarAsaltante(asaltante);

        String numeroBanda = asaltante.getBanda().getNumeroBanda();

        Banda banda = bandaRepository
                .buscarParaRegistrarIntegrante(numeroBanda)
                .orElseThrow(() -> new IllegalArgumentException(
                    "No existe la banda número '"
                    + numeroBanda + "'"
                ));

        if (asaltanteRepository.existsById(asaltante.getClave())) {
            throw new IllegalArgumentException(
                    "La clave de asaltante '"
                    + asaltante.getClave()
                    + "' ya está registrada"
            );
        }

        // El bloqueo de la banda evita superar el límite con registros simultáneos.
        long integrantes = asaltanteRepository.countByBanda_NumeroBanda(numeroBanda);
        if (integrantes >= banda.getCantMiembros()) {
            throw new IllegalArgumentException(
                    "La banda '" + numeroBanda + "' ya está completa: tiene "
                    + integrantes + " integrantes registrados y admite "
                    + banda.getCantMiembros() + ".");
        }

        asaltante.setBanda(banda);

        return asaltanteRepository.save(asaltante);
    }

    private void validarAsaltante(Asaltante asaltante) {

        if (asaltante == null) {
            throw new IllegalArgumentException(
                    "Los datos del asaltante son obligatorios"
            );
        }

        if (asaltante.getClave() == null
                || asaltante.getClave().isBlank()) {

            throw new IllegalArgumentException(
                    "La clave del asaltante es obligatoria"
            );
        }

        if (asaltante.getNombreCompleto() == null
                || asaltante.getNombreCompleto().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre del asaltante es obligatorio"
            );
        }

        if (asaltante.getBanda() == null
                || asaltante.getBanda().getNumeroBanda() == null
                || asaltante.getBanda().getNumeroBanda().isBlank()) {

            throw new IllegalArgumentException(
                    "El número de banda es obligatorio"
            );
        }
    }
}
