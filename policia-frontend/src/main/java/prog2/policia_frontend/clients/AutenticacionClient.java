package prog2.policia_frontend.clients;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.UsuarioDTO;
import prog2.policia_frontend.security.ResultadoAutenticacion;

@Service
public class AutenticacionClient {

    private final RestClient restClient;

    public AutenticacionClient(@Value("${backend.url}") String backendUrl) {
        this.restClient = RestClient.builder().baseUrl(backendUrl).build();
    }

    public ResultadoAutenticacion autenticar(String codigo, String password) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Debe ingresar su código.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Debe ingresar su contraseña.");
        }

        String codigoNormalizado = codigo.trim().toUpperCase();

        UsuarioDTO usuario = restClient.get()
                .uri("/usuarios/mi-perfil")
                .headers(headers -> headers.setBasicAuth(codigoNormalizado, password))
                .retrieve()
                .body(UsuarioDTO.class);

        List<String> permisosRecibidos = restClient.get()
                .uri("/usuarios/mis-permisos")
                .headers(headers -> headers.setBasicAuth(codigoNormalizado, password))
                .retrieve()
                .body(new ParameterizedTypeReference<List<String>>() {
                });

        Set<String> permisos = new HashSet<>();

        if (permisosRecibidos != null) {
            permisos.addAll(permisosRecibidos);
        }

        return new ResultadoAutenticacion(usuario, permisos);
    }
}
