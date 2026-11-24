package prog2.policia_backend.DTOs;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import prog2.policia_backend.models.EstadoCasoJudicial;
import prog2.policia_backend.models.EstadoParticipacionJudicial;

/** El resultado judicial corresponde exclusivamente al defendido consultado. */
@Getter
@AllArgsConstructor
public class CasoDefensaDTO {
    private String codigo;
    private String defendidoCodigo;
    private String asaltoCodigo;
    private LocalDate fechaAsalto;
    private String juezCodigo;
    private boolean activo;
    private EstadoCasoJudicial estado;
    private EstadoParticipacionJudicial situacionJudicial;
    private boolean sentenciado;
    // Sin sentencia, estos valores son null: pendiente no significa absuelto.
    private Boolean condenado;
    private Integer tiempoCarcel;
}
