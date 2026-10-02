package prog2.policia_backend.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JuezDTO {

    private Long id;
    
    @NotBlank
    private String nombre;
    
    @NotNull
    @PastOrPresent
    private LocalDate juezDesde;
    
    @Min(0)
    private int aniosServicio;
    private String codigo;
}