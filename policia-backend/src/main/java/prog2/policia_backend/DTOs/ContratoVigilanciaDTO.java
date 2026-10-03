package prog2.policia_backend.DTOs;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
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
    private Boolean conArma;

    @NotNull
    private Long vigilanteId;

    @NotNull
    private Long sucursalId;
    private String codigo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean activo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private MotivoBajaContrato motivoBaja;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaCreacion;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaModificacion;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String creadoPor;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String modificadoPor;
}
