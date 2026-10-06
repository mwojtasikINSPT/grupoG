package prog2.policia_backend.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import prog2.policia_backend.models.MotivoBajaEntidadBancaria;
import prog2.policia_backend.validations.OnCreate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntidadBancariaDTO {

    private Long id;

    @NotBlank(groups = {OnCreate.class})
    private String domicilioCentral;
    private String codigo;

    @NotBlank(groups = {OnCreate.class})
    private String nombre;

    private MotivoBajaEntidadBancaria motivoBaja;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean activo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaCreacion;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaModificacion;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String creadoPor;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String modificadoPor;
}
