package prog2.policia_backend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VigilanteDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String password;
    private int edad;
}