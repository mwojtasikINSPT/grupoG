package grupog.sistemapolicia.dto;

import java.time.LocalDate;

public record AsaltoRequest(
        String idAsalto,
        String claveAsaltante,
        String codigoSucursal,
        LocalDate fecha) {
}
