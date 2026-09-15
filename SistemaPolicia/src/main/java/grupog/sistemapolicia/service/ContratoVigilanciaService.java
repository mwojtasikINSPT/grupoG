package grupog.sistemapolicia.service;

import grupog.sistemapolicia.model.ContratoVigilancia;
import grupog.sistemapolicia.model.Sucursal;
import grupog.sistemapolicia.model.Vigilante;
import grupog.sistemapolicia.repository.ContratoVigilanciaRepository;
import grupog.sistemapolicia.repository.SucursalRepository;
import grupog.sistemapolicia.repository.VigilanteRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ContratoVigilanciaService {

    private final ContratoVigilanciaRepository contratoRepository;
    private final SucursalRepository sucursalRepository;
    private final VigilanteRepository vigilanteRepository;

    public ContratoVigilanciaService(
            ContratoVigilanciaRepository contratoRepository,
            SucursalRepository sucursalRepository,
            VigilanteRepository vigilanteRepository) {

        this.contratoRepository = contratoRepository;
        this.sucursalRepository = sucursalRepository;
        this.vigilanteRepository = vigilanteRepository;
    }

    public List<ContratoVigilancia> listar() {
        return contratoRepository.findAll();
    }

    public ContratoVigilancia guardar(
            ContratoVigilancia contrato) {

        validarContrato(contrato);

        if (contratoRepository.existsById(
                contrato.getCodigo())) {

            throw new IllegalArgumentException(
                    "El contrato '" + contrato.getCodigo()
                    + "' ya existe"
            );
        }

        String codigoSucursal =
                contrato.getSucursal().getCodigo();

        String codigoVigilante =
                contrato.getVigilante().getCodigo();

        Sucursal sucursal = sucursalRepository
                .findById(codigoSucursal)
                .orElseThrow(() -> new IllegalArgumentException(
                    "No existe la sucursal '"
                    + codigoSucursal + "'"
                ));

        Vigilante vigilante = vigilanteRepository
                .findById(codigoVigilante)
                .orElseThrow(() -> new IllegalArgumentException(
                    "No existe el vigilante '"
                    + codigoVigilante + "'"
                ));

        contrato.setSucursal(sucursal);
        contrato.setVigilante(vigilante);

        return contratoRepository.save(contrato);
    }

    private void validarContrato(
            ContratoVigilancia contrato) {

        if (contrato == null) {
            throw new IllegalArgumentException(
                    "Los datos del contrato son obligatorios"
            );
        }

        if (contrato.getCodigo() == null
                || contrato.getCodigo().isBlank()) {

            throw new IllegalArgumentException(
                    "El código del contrato es obligatorio"
            );
        }

        if (contrato.getSucursal() == null
                || contrato.getSucursal().getCodigo() == null
                || contrato.getSucursal()
                           .getCodigo()
                           .isBlank()) {

            throw new IllegalArgumentException(
                    "El código de la sucursal es obligatorio"
            );
        }

        if (contrato.getVigilante() == null
                || contrato.getVigilante().getCodigo() == null
                || contrato.getVigilante()
                           .getCodigo()
                           .isBlank()) {

            throw new IllegalArgumentException(
                    "El código del vigilante es obligatorio"
            );
        }

        if (contrato.getFecha() == null) {
            throw new IllegalArgumentException(
                    "La fecha del contrato es obligatoria"
            );
        }
    }
}