package prog2.policia_backend.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambioPasswordDTO {

    @NotBlank
    private String passwordActual;

    @NotBlank
    private String passwordNueva;
}