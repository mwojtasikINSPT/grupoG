package prog2.policia_backend.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private int edad;
    
}