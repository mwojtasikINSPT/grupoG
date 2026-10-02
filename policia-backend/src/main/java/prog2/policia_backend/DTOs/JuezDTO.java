package prog2.policia_backend.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

import prog2.policia_backend.models.MotivoBajaPersona;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JuezDTO {

    private Long id;

    @NotBlank
    private String nombre;

    @NotNull
    @PastOrPresent
    private LocalDate juezDesde;

    @Min(0)
    private int aniosServicio;
    private String codigo;
    private MotivoBajaPersona motivoBaja;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaCreacion;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaModificacion;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String creadoPor;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String modificadoPor;
}
