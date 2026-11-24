package prog2.policia_backend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResultadoAsignacionAbogadoDTO {
    private String asaltanteCodigo;
    private String abogadoAnteriorCodigo;
    private String abogadoDefensorCodigo;
    private String mensaje;
}
