package prog2.policia_backend.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import prog2.policia_backend.models.MotivoBajaPersona;
import prog2.policia_backend.validations.OnCreate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;
    private String codigo;

    @NotBlank(groups = OnCreate.class)
    private String nombre;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(groups = OnCreate.class)
    private String password;

    @NotBlank(groups = OnCreate.class)
    private String rol;

    private MotivoBajaPersona motivoBaja;

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
