package prog2.policia_frontend.clients;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.dtos.RolDTO;
import prog2.policia_frontend.dtos.UsuarioDTO;

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

    public UsuarioDTO autenticar(
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

        try {

            /*
             * Es la ruta principal. No depende del prefijo,
             * por lo que admite usuarios de roles nuevos.
             */
            usuario = consultarUsuario(
                    "/usuarios/{codigo}",
                    codigoNormalizado,
                    password
            );

        } catch (RestClientResponseException excepcion) {

            /*
             * Los vigilantes actuales solo tienen permiso para
             * consultar sus propios datos por su ruta específica.
             * Se conserva compatibilidad con los tres tipos viejos.
             */
            int estado = excepcion.getStatusCode().value();

            if ((estado == 403 || estado == 404)
                    && esPrefijoAnterior(codigoNormalizado)) {

                usuario = consultarUsuario(
                        obtenerEndpointAnterior(codigoNormalizado),
                        codigoNormalizado,
                        password
                );

            } else {
                throw excepcion;
            }
        }

        cargarPermisosDelRol(
                usuario,
                codigoNormalizado,
                password
        );

        return usuario;
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
    private void cargarPermisosDelRol(
            UsuarioDTO usuario,
            String codigo,
            String password) {

        if (usuario == null
                || usuario.getRol() == null
                || usuario.getRol().isBlank()) {
            return;
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
                return;
            }

            roles.stream()
                    .filter(rol
                            -> rol.getNombre() != null
                            && rol.getNombre().equalsIgnoreCase(
                                    usuario.getRol()
                            ))
                    .findFirst()
                    .ifPresent(rol
                            -> usuario.setPermisos(rol.getPermisos()));

        } catch (RestClientResponseException excepcion) {

            /*
             * No se interrumpe un login válido solamente porque
             * ese usuario no tenga permiso para listar roles.
             */
        }
    }

    private boolean esPrefijoAnterior(String codigo) {
        return codigo.startsWith("ADM")
                || codigo.startsWith("INV")
                || codigo.startsWith("VIG");
    }

    private String obtenerEndpointAnterior(String codigo) {

        if (codigo.startsWith("ADM")) {
            return "/administradores/{codigo}";
        }

        if (codigo.startsWith("INV")) {
            return "/investigadores/{codigo}";
        }

        return "/vigilantes/{codigo}";
    }
}
