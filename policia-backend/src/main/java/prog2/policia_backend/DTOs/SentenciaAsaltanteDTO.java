package prog2.policia_backend.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import prog2.policia_backend.validations.OnCreate;

// Transporta los datos individuales de una sentencia judicial.
@Data
@NoArgsConstructor
public class SentenciaAsaltanteDTO {

    @NotBlank(groups = OnCreate.class)
    private String asaltanteCodigo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean sentenciado;

    @NotNull
    private Boolean condenado;

    @NotNull
    @Min(0)
    private Integer tiempoCarcel;
}
