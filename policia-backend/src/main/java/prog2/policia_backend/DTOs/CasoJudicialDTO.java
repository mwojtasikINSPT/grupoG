package prog2.policia_backend.DTOs;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import prog2.policia_backend.validations.OnCreate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasoJudicialDTO {

    private Long id;

    private Boolean sentenciado;
    private Boolean condenado;

    @Min(0)
    private Integer tiempoCarcel;

    @NotBlank(groups = {OnCreate.class})
    private String asaltoCodigo;

    @NotEmpty(groups = {OnCreate.class})
    private List<String> asaltanteCodigos = new ArrayList<>();

    @NotEmpty(groups = {OnCreate.class})
    private List<String> juezCodigos = new ArrayList<>();

    private String codigo;

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
