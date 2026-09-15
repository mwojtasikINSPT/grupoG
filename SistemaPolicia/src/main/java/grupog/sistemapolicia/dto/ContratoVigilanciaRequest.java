package grupog.sistemapolicia.dto;

import java.time.LocalDate;

public record ContratoVigilanciaRequest(
        String codigo,
        String codigoSucursal,
        String codigoVigilante,
        LocalDate fecha,
        boolean conArma) {
}