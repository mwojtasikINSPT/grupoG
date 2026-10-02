package prog2.policia_backend.DTOs;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import prog2.policia_backend.models.MotivoBajaContrato;

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

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean activo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private MotivoBajaContrato motivoBaja;
}
