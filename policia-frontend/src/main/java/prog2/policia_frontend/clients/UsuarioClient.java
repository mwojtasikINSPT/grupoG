
package prog2.policia_frontend.clients;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.UsuarioDTO;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.dtos.BajaUsuarioDTO;
import prog2.policia_frontend.dtos.CambioPasswordDTO;
import prog2.policia_frontend.dtos.UsuarioFormularioDTO;

/*
 * Se comunica con los endpoints de usuarios
 * disponibles en el backend.
 */
@Service
@VaadinSessionScope
public class UsuarioClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    /*
     * Recibe la dirección del backend
     * y la sesión del usuario conectado.
     */
    public UsuarioClient(
            @Value("${backend.url}") String backendUrl,
            SesionUsuario sesionUsuario) {

        this.restClient = RestClient.builder()
                .baseUrl(backendUrl)
                .build();

        this.sesionUsuario = sesionUsuario;
    }

    /*
     * Consulta los usuarios registrados.
     *
     * activo:
     * - null: todos;
     * - true: activos;
     * - false: inactivos.
     *
     * nombre:
     * - null o vacío: no filtra por nombre;
     * - con contenido: busca coincidencias.
     */
    public List<UsuarioDTO> listar(
            Boolean activo,
            String nombre) {

        List<UsuarioDTO> usuarios = restClient.get()
                .uri(constructor -> {

                    constructor.path("/usuarios");

                    if (activo != null) {
                        constructor.queryParam(
                                "activo",
                                activo
                        );
                    }

                    if (nombre != null
                            && !nombre.isBlank()) {

                        constructor.queryParam(
                                "nombre",
                                nombre.trim()
                        );
                    }

                    return constructor.build();
                })
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference
                                <List<UsuarioDTO>>() {
                        }
                );

        /*
         * Si el backend no devuelve contenido,
         * entrega una lista vacía y evita un null.
         */
        return usuarios != null
                ? usuarios
                : List.of();
    }
    /*
 * Solicita la baja lógica de un usuario.
 *
 * El usuario continúa guardado, pero pasa
 * a tener el estado inactivo.
 */
public void darDeBaja(
        String codigo,
        String motivoBaja) {

    if (codigo == null || codigo.isBlank()) {
        throw new IllegalArgumentException(
                "El código del usuario es obligatorio."
        );
    }

    if (motivoBaja == null
            || motivoBaja.isBlank()) {

        throw new IllegalArgumentException(
                "Debe seleccionar un motivo de baja."
        );
    }

    BajaUsuarioDTO datosBaja
            = new BajaUsuarioDTO(motivoBaja);

    restClient.patch()
            .uri(
                    "/usuarios/{codigo}/baja",
                    codigo.trim().toUpperCase()
            )
            .headers(
                    sesionUsuario::aplicarAutenticacion
            )
            .body(datosBaja)
            .retrieve()
            .toBodilessEntity();
}

/*
 * Solicita la reactivación de un usuario
 * que actualmente se encuentra inactivo.
 */
public UsuarioDTO reactivar(
        String codigo) {

    if (codigo == null || codigo.isBlank()) {
        throw new IllegalArgumentException(
                "El código del usuario es obligatorio."
        );
    }

    return restClient.patch()
            .uri(
                    "/usuarios/{codigo}/reactivar",
                    codigo.trim().toUpperCase()
            )
            .headers(
                    sesionUsuario::aplicarAutenticacion
            )
            .retrieve()
            .body(UsuarioDTO.class);
}
/*
 * Envía al backend los datos necesarios
 * para crear un usuario nuevo.
 */
public UsuarioDTO crear(
        UsuarioFormularioDTO datos) {

    if (datos == null) {
        throw new IllegalArgumentException(
                "Los datos del usuario son obligatorios."
        );
    }

    return restClient.post()
            .uri("/usuarios")
            .headers(
                    sesionUsuario::aplicarAutenticacion
            )
            .body(datos)
            .retrieve()
            .body(UsuarioDTO.class);
}

/*
 * Envía modificaciones parciales
 * para un usuario existente.
 *
 * Una contraseña vacía no debería cambiar
 * la contraseña actual del usuario.
 */
public UsuarioDTO actualizar(
        String codigo,
        UsuarioFormularioDTO datos) {

    if (codigo == null || codigo.isBlank()) {
        throw new IllegalArgumentException(
                "El código del usuario es obligatorio."
        );
    }

    if (datos == null) {
        throw new IllegalArgumentException(
                "Los datos del usuario son obligatorios."
        );
    }

    return restClient.put()
            .uri(
                    "/usuarios/{codigo}",
                    codigo.trim().toUpperCase()
            )
            .headers(
                    sesionUsuario::aplicarAutenticacion
            )
            .body(datos)
            .retrieve()
            .body(UsuarioDTO.class);
}

/**
 * Cambia la contraseña del usuario que se encuentra autenticado.
 */
public void cambiarPasswordPropia(
        String passwordActual,
        String passwordNueva) {

    if (passwordActual == null || passwordActual.isBlank()) {
        throw new IllegalArgumentException(
                "La contraseña actual es obligatoria."
        );
    }

    if (passwordNueva == null || passwordNueva.isBlank()) {
        throw new IllegalArgumentException(
                "La nueva contraseña es obligatoria."
        );
    }

    CambioPasswordDTO datos = new CambioPasswordDTO(
            passwordActual,
            passwordNueva
    );

    restClient.patch()
            .uri("/usuarios/password")
            .headers(sesionUsuario::aplicarAutenticacion)
            .body(datos)
            .retrieve()
            .toBodilessEntity();
}
}
