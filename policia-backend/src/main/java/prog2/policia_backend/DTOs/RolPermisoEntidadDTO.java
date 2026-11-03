package prog2.policia_backend.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import prog2.policia_backend.models.Entidad;
import prog2.policia_backend.models.Permiso;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolPermisoEntidadDTO {

    @NotNull
    private Permiso permiso;

    @NotNull
    private Entidad entidad;
}
