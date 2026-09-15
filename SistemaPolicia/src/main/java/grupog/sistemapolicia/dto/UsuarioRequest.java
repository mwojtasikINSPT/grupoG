package grupog.sistemapolicia.dto;

import grupog.sistemapolicia.model.Rol;

public record UsuarioRequest(
        String username,
        String password,
        Rol rol,
        String codigoVigilante) {
}
