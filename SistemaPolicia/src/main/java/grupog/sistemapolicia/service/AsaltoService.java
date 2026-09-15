package grupog.sistemapolicia.service;

import grupog.sistemapolicia.model.Asaltante;
import grupog.sistemapolicia.model.Asalto;
import grupog.sistemapolicia.model.Sucursal;
import grupog.sistemapolicia.repository.AsaltanteRepository;
import grupog.sistemapolicia.repository.AsaltoRepository;
import grupog.sistemapolicia.repository.SucursalRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AsaltoService {

    private final AsaltoRepository asaltoRepository;
    private final AsaltanteRepository asaltanteRepository;
    private final SucursalRepository sucursalRepository;

    public AsaltoService(AsaltoRepository asaltoRepository,
            AsaltanteRepository asaltanteRepository,
            SucursalRepository sucursalRepository) {
        this.asaltoRepository = asaltoRepository;
        this.asaltanteRepository = asaltanteRepository;
        this.sucursalRepository = sucursalRepository;
    }

    public List<Asalto> listar() {
        return asaltoRepository.findAll();
    }

    public Asalto guardar(Asalto asalto) {
        validar(asalto);

        if (asaltoRepository.existsById(asalto.getIdAsalto())) {
            throw new IllegalArgumentException(
                    "El asalto '" + asalto.getIdAsalto() + "' ya existe");
        }

        String claveAsaltante = asalto.getAsaltante().getClave();
        String codigoSucursal = asalto.getSucursal().getCodigo();

        Asaltante asaltante = asaltanteRepository.findById(claveAsaltante)
                .orElseThrow(() -> new IllegalArgumentException(
                "No existe el asaltante '" + claveAsaltante + "'"));

        Sucursal sucursal = sucursalRepository.findById(codigoSucursal)
                .orElseThrow(() -> new IllegalArgumentException(
                "No existe la sucursal '" + codigoSucursal + "'"));

        asalto.setAsaltante(asaltante);
        asalto.setSucursal(sucursal);
        return asaltoRepository.save(asalto);
    }

    private void validar(Asalto asalto) {
        if (asalto == null) {
            throw new IllegalArgumentException(
                    "Los datos del asalto son obligatorios");
        }
        if (asalto.getIdAsalto() == null
                || asalto.getIdAsalto().isBlank()) {
            throw new IllegalArgumentException(
                    "El identificador del asalto es obligatorio");
        }
        if (asalto.getAsaltante() == null
                || asalto.getAsaltante().getClave() == null
                || asalto.getAsaltante().getClave().isBlank()) {
            throw new IllegalArgumentException(
                    "La clave del asaltante es obligatoria");
        }
        if (asalto.getSucursal() == null
                || asalto.getSucursal().getCodigo() == null
                || asalto.getSucursal().getCodigo().isBlank()) {
            throw new IllegalArgumentException(
                    "El código de la sucursal es obligatorio");
        }
        if (asalto.getFecha() == null) {
            throw new IllegalArgumentException(
                    "La fecha del asalto es obligatoria");
        }
    }
}
