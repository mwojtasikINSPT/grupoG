package prog2.policia_backend.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SucursalDTO {

    private Long id;
    
    @NotBlank
    private String domicilio;
    
    @Min(0)
    private int cantEmpleados;
    
    @NotNull
    private Long entidadBancariaId;
    private String codigo;
}