package prog2.policia_backend.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import prog2.policia_backend.validations.OnCreate;

// Transporta los datos de un caso judicial entre las capas.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasoJudicialDTO {

    private Long id;

    private String codigo;

    private Boolean sentenciado;

    private Boolean activo;

    @NotBlank(groups = {OnCreate.class})
    private String asaltoCodigo;

    @NotBlank(groups = {OnCreate.class})
    private String juezCodigo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<String> asaltanteCodigos = new ArrayList<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaCreacion;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaModificacion;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String creadoPor;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String modificadoPor;
}