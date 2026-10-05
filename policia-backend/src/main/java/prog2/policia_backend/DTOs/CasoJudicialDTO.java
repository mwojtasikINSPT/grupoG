package prog2.policia_backend.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasoJudicialDTO {

    private Long id;
    private boolean condenado;

    @Min(0)
    private int tiempoCarcel;

    private boolean sentenciado;

    @NotBlank
    private String asaltoCodigo;
    
    @NotBlank
    private String asaltanteCodigo;
    
    @NotBlank
    private String juezCodigo;

    private String codigo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaCreacion;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaModificacion;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String creadoPor;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String modificadoPor;
}
