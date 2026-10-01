package prog2.policia_backend.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JuezDTO {

    private Long id;
    private String nombre;
    private LocalDate juezDesde;
    private int aniosServicio;
    private String codigo;
}