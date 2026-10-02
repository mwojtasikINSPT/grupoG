package prog2.policia_backend.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import prog2.policia_backend.models.MotivoBajaAsaltante;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsaltanteDTO {

    private Long id;

    @NotBlank
    private String nombre;
    private Long bandaId;
    private String codigo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private MotivoBajaAsaltante motivoBaja;
}
