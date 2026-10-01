package prog2.policia_backend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsaltanteDTO {

    private Long id;
    private String nombre;
    private Long bandaId;
    private String codigo;
}