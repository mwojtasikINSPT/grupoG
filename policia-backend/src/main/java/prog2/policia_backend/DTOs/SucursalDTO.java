package prog2.policia_backend.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import prog2.policia_backend.models.MotivoCierreSucursal;
import prog2.policia_backend.validations.OnCreate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SucursalDTO {

    private Long id;

    @NotBlank(groups = {OnCreate.class})
    private String domicilio;

    @NotNull(groups = {OnCreate.class})
    @Min(0)
    private Integer cantEmpleados;

    @NotBlank
    private String entidadBancariaCodigo;
    private String codigo;
    private MotivoCierreSucursal motivoCierre;

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
