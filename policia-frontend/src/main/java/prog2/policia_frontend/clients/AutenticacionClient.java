package prog2.policia_frontend.clients;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.dtos.RolDTO;
import prog2.policia_frontend.dtos.UsuarioDTO;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.ResultadoAutenticacion;

/*
 * Comprueba las credenciales contra el backend y obtiene
 * los datos necesarios para iniciar la sesión de Vaadin.
 */
@Service
public class AutenticacionClient {

    private final RestClient restClient;

    public AutenticacionClient(
            @Value("${backend.url}") String backendUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(backendUrl)
                .build();
    }

    public ResultadoAutenticacion autenticar(
            String codigo,
            String password) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe ingresar su código."
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe ingresar su contraseña."
            );
        }

        String codigoNormalizado
                = codigo.trim().toUpperCase();

        UsuarioDTO usuario;
        boolean accesoSoloPropio = false;

        try {

            usuario = consultarUsuario(
                    "/usuarios/{codigo}",
                    codigoNormalizado,
                    password
            );

        } catch (RestClientResponseException excepcion) {

            int estado = excepcion.getStatusCode().value();

            if (estado == 404) {

                usuario = consultarUsuario(
                        "/vigilantes/{codigo}",
                        codigoNormalizado,
                        password
                );
                accesoSoloPropio = true;

            } else {
                throw excepcion;
            }
        }

        Set<String> permisos = cargarPermisosDelRol(
                usuario,
                codigoNormalizado,
                password
        );

        if (permisos.isEmpty() && accesoSoloPropio) {
            permisos.add(PermisosApp.LECTURA_DATOS_PROPIOS);
        }

        return new ResultadoAutenticacion(usuario, permisos);
    }

    private UsuarioDTO consultarUsuario(
            String endpoint,
            String codigo,
            String password) {

        return restClient.get()
                .uri(endpoint, codigo)
                .headers(headers
                        -> headers.setBasicAuth(codigo, password))
                .retrieve()
                .body(UsuarioDTO.class);
    }

    /*
     * Si el usuario tiene lectura general, el backend permite
     * consultar los roles y completar sus permisos en la sesión.
     * Si no puede hacerlo, el login igualmente se conserva.
     */
    private Set<String> cargarPermisosDelRol(
            UsuarioDTO usuario,
            String codigo,
            String password) {

        Set<String> permisos = new HashSet<>();

        if (usuario == null
                || usuario.getRol() == null
                || usuario.getRol().isBlank()) {
            return permisos;
        }

        try {

            List<RolDTO> roles = restClient.get()
                    .uri("/roles")
                    .headers(headers
                            -> headers.setBasicAuth(codigo, password))
                    .retrieve()
                    .body(
                            new ParameterizedTypeReference
                                    <List<RolDTO>>() {
                            }
                    );

            if (roles == null) {
                return permisos;
            }

            roles.stream()
                    .filter(rol
                            -> rol.getNombre() != null
                            && rol.getNombre().equalsIgnoreCase(
                                    usuario.getRol()
                            ))
                    .findFirst()
                    .ifPresent(rol
                            -> {
                        if (rol.getPermisos() != null) {
                            permisos.addAll(rol.getPermisos());
                        }
                    });

        } catch (RestClientResponseException excepcion) {

            /*
             * No se interrumpe un login válido solamente porque
             * ese usuario no tenga permiso para listar roles.
             */
            permisos.add(PermisosApp.LECTURA_DATOS_PROPIOS);
        }

        return permisos;
    }
}
