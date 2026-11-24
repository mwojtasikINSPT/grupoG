package prog2.policia_backend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import prog2.policia_backend.models.MotivoBajaPersona;

/** Datos personales del defendido, sin relaciones con terceros ni auditoria. */
@Getter
@AllArgsConstructor
public class DefendidoDTO {
    private String codigo;
    private String nombre;
    private boolean activo;
    private MotivoBajaPersona motivoBaja;
}
