package prog2.policia_backend.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import prog2.policia_backend.models.MotivoBajaPersona;
import prog2.policia_backend.validations.OnCreate;
import prog2.policia_backend.validations.OnUpdate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvestigadorDTO {

    private Long id;
    private String codigo;

    @NotBlank(groups = OnCreate.class)
    private String nombre;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(groups = OnCreate.class)
    @Size(min = 4, max = 20, groups = {OnCreate.class, OnUpdate.class})
    private String password;
    private MotivoBajaPersona motivoBaja;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String rol;

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
