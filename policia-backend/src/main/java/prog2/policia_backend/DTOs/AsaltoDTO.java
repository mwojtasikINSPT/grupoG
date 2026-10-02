package prog2.policia_backend.DTOs;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsaltoDTO {

    private Long id;

    @NotNull
    private LocalDate fecha;

    @NotEmpty
    private List<Long> asaltantesIds;
    
    @NotNull
    private Long sucursalId;
    private String codigo;
}
