package prog2.policia_backend.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import prog2.policia_backend.validations.OnCreate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolDTO {

    private Long id;
    private String codigo;

    @NotBlank(groups = OnCreate.class)
    private String nombre;

    @NotBlank(groups = OnCreate.class)
    private String prefijo;

    @Valid
    @NotEmpty(groups = OnCreate.class)
    private Set<RolPermisoEntidadDTO> permisosPorEntidad;

    private String descripcion;

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
