package prog2.policia_backend.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PresentacionPruebaDefensaDTO {
    @NotBlank
    private String asaltanteCodigo;

    @NotBlank
    private String casoCodigo;

    @NotBlank
    @Size(max = 4000)
    private String descripcion;
}
