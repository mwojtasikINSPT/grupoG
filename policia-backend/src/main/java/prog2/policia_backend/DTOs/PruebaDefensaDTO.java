package prog2.policia_backend.DTOs;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PruebaDefensaDTO {
    private Long id;
    private String descripcion;
    private LocalDateTime fechaPresentacion;
    private String abogadoCodigo;
    private String asaltanteCodigo;
    private String casoCodigo;
}
