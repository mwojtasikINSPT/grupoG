
package prog2.policia_frontend.clients;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.RolDTO;
import prog2.policia_frontend.security.SesionUsuario;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
/*
 * Se comunica con los endpoints de roles
 * disponibles en el backend.
 */
@Service
@VaadinSessionScope
public class RolClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    /*
     * Spring entrega la dirección del backend
     * y la sesión del usuario conectado.
     */
    public RolClient(
            @Value("${backend.url}") String backendUrl,
            SesionUsuario sesionUsuario) {

        this.restClient = RestClient.builder()
                .baseUrl(backendUrl)
                .build();

        this.sesionUsuario = sesionUsuario;
    }

    /*
     * Consulta todos los roles.
     *
     * Si activo es:
     * - null: devuelve activos e inactivos;
     * - true: devuelve solamente activos;
     * - false: devuelve solamente inactivos.
     */
    public List<RolDTO> listar(Boolean activo) {

        String endpoint = activo == null
                ? "/roles"
                : "/roles?activo=" + activo;

        List<RolDTO> roles = restClient.get()
                .uri(endpoint)
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference
                                <List<RolDTO>>() {
                        }
                );

        /*
         * Si el backend no devuelve una lista,
         * entregamos una lista vacía para evitar null.
         */
        return roles != null
                ? roles
                : List.of();
    }

    public RolDTO crear(RolDTO datos) {

        validarDatos(datos);

        return restClient.post()
                .uri("/roles")
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(RolDTO.class);
    }

    public RolDTO actualizar(
            String codigo,
            RolDTO datos) {

        validarCodigo(codigo);
        validarDatos(datos);

        return restClient.put()
                .uri(
                        "/roles/{codigo}",
                        codigo.trim().toUpperCase()
                )
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(RolDTO.class);
    }

    public void darDeBaja(String codigo) {

        validarCodigo(codigo);

        restClient.patch()
                .uri(
                        "/roles/{codigo}/baja",
                        codigo.trim().toUpperCase()
                )
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .toBodilessEntity();
    }

    public RolDTO reactivar(String codigo) {

        validarCodigo(codigo);

        return restClient.patch()
                .uri(
                        "/roles/{codigo}/reactivar",
                        codigo.trim().toUpperCase()
                )
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(RolDTO.class);
    }

    private void validarDatos(RolDTO datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del rol son obligatorios."
            );
        }

        if (datos.getNombre() == null
                || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del rol es obligatorio."
            );
        }

        if (datos.getPrefijo() == null
                || datos.getPrefijo().isBlank()) {
            throw new IllegalArgumentException(
                    "El prefijo del rol es obligatorio."
            );
        }

        if (datos.getPermisos() == null
                || datos.getPermisos().isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un permiso."
            );
        }
    }

    private void validarCodigo(String codigo) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del rol es obligatorio."
            );
        }
    }
}
