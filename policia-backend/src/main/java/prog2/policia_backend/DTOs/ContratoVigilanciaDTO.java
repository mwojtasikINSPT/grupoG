package prog2.policia_backend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratoVigilanciaDTO {

    private Long id;
    private LocalDate fecha;
    private boolean conArma;
    private Long vigilanteId;
    private Long sucursalId;
    private String codigo;
}