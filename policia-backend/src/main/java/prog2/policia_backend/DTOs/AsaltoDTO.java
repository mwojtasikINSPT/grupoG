package prog2.policia_backend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsaltoDTO {

    private Long id;
    private LocalDate fecha;
    private Long asaltanteId;
    private Long sucursalId;
    private String codigo;
}