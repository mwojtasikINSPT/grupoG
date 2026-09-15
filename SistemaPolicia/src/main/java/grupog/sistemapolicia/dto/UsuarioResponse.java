package grupog.sistemapolicia.dto;

import grupog.sistemapolicia.model.Rol;

public record UsuarioResponse(
        String username,
        Rol rol,
        String codigoVigilante) {
}
