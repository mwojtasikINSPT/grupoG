package prog2.policia_backend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResultadoBajaUsuarioDTO {
    private String codigo;
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private Integer defendidosDesvinculados;
    private String mensaje;
}
