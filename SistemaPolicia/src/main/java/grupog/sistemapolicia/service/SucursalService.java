package grupog.sistemapolicia.service;

import grupog.sistemapolicia.model.EntidadBancaria;
import grupog.sistemapolicia.model.Sucursal;
import grupog.sistemapolicia.repository.EntidadBancariaRepository;
import grupog.sistemapolicia.repository.SucursalRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SucursalService {

    private final SucursalRepository sucursalRepository;
    private final EntidadBancariaRepository entidadRepository;

    public SucursalService(
            SucursalRepository sucursalRepository,
            EntidadBancariaRepository entidadRepository) {

        this.sucursalRepository = sucursalRepository;
        this.entidadRepository = entidadRepository;
    }

    public List<Sucursal> listar() {
        return sucursalRepository.findAll();
    }

    public Sucursal guardar(Sucursal sucursal) {

        validarSucursal(sucursal);

        if (sucursalRepository.existsById(sucursal.getCodigo())) {
            throw new IllegalArgumentException(
                    "La sucursal '" + sucursal.getCodigo()
                    + "' ya existe"
            );
        }

        String codigoEntidad =
                sucursal.getEntidad().getCodigo();

        EntidadBancaria entidad = entidadRepository
                .findById(codigoEntidad)
                .orElseThrow(() -> new IllegalArgumentException(
                    "No existe la entidad bancaria '"
                    + codigoEntidad + "'"
                ));

        sucursal.setEntidad(entidad);

        return sucursalRepository.save(sucursal);
    }

    private void validarSucursal(Sucursal sucursal) {

        if (sucursal == null) {
            throw new IllegalArgumentException(
                    "Los datos de la sucursal son obligatorios"
            );
        }

        if (sucursal.getCodigo() == null
                || sucursal.getCodigo().isBlank()) {

            throw new IllegalArgumentException(
                    "El código de sucursal es obligatorio"
            );
        }

        if (sucursal.getDomicilio() == null
                || sucursal.getDomicilio().isBlank()) {

            throw new IllegalArgumentException(
                    "El domicilio es obligatorio"
            );
        }

        if (sucursal.getNumeroEmpleados() < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de empleados no puede ser negativa"
            );
        }

        if (sucursal.getEntidad() == null
                || sucursal.getEntidad().getCodigo() == null
                || sucursal.getEntidad().getCodigo().isBlank()) {

            throw new IllegalArgumentException(
                    "El código de la entidad bancaria es obligatorio"
            );
        }
    }
}