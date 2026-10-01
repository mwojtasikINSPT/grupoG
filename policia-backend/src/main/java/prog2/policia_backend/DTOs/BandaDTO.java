package prog2.policia_backend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BandaDTO {

    private Long id;
    private int cantMiembros;
    private String codigo;
}