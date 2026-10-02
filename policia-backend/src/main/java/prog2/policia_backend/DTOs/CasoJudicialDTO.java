package prog2.policia_backend.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasoJudicialDTO {

    private Long id;
    private boolean condenado;

    @Min(0)
    private int tiempoCarcel;

    @NotNull
    private Long asaltoId;

    @NotNull
    private Long asaltanteId;

    @NotNull
    private Long juezId;

    private String codigo;
}
