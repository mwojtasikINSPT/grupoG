package prog2.policia_backend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResultadoBajaUsuarioDTO {
    private String codigo;
    private int defendidosDesvinculados;
    private String mensaje;
}
