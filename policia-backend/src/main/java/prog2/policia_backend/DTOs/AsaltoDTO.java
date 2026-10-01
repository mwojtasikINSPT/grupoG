package prog2.policia_backend.DTOs;

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
    private LocalDate fecha;
    private List<Long> asaltantesIds;
    private Long sucursalId;
    private String codigo;
}