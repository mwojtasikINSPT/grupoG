package prog2.policia_backend.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import prog2.policia_backend.models.MotivoBajaPersona;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsaltanteDTO {

    private Long id;

    @NotBlank
    private String nombre;
    private String bandaCodigo;
    private String codigo;

    private MotivoBajaPersona motivoBaja;

    private Boolean quitarDeBanda;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaCreacion;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaModificacion;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String creadoPor;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String modificadoPor;

}
