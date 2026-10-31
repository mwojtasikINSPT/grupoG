package prog2.policia_backend.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prog2.policia_backend.validations.OnCreate;

// Transporta los datos individuales de una sentencia judicial.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentenciaAsaltanteDTO {

    @NotBlank(groups = OnCreate.class)
    private String asaltanteCodigo;

    private boolean sentenciado;

    @NotNull
    private Boolean condenado;

    @NotNull
    @Min(0)
    private Integer tiempoCarcel;
}
