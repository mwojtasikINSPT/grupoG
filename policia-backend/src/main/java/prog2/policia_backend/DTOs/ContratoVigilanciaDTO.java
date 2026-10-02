package prog2.policia_backend.DTOs;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratoVigilanciaDTO {

    private Long id;
    
    @FutureOrPresent
    @NotNull
    private LocalDate fecha;
    private boolean conArma;

    @NotNull
    private Long vigilanteId;

    @NotNull
    private Long sucursalId;
    private String codigo;
}
