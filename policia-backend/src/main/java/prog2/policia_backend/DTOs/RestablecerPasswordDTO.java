package prog2.policia_backend.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestablecerPasswordDTO {

    @NotBlank
    private String passwordNueva;
}