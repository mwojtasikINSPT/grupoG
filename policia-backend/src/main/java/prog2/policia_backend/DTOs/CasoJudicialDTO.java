package prog2.policia_backend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasoJudicialDTO {

    private Long id;
    private boolean condenado;
    private int tiempoCarcel;
    private Long asaltoId;
    private Long juezId;
    private String codigo;
}