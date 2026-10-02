package prog2.policia_backend.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntidadBancariaDTO {

    private Long id;
    
    @NotBlank
    private String domicilioCentral;
    private String codigo;
    
    @NotBlank
    private String nombre;
}