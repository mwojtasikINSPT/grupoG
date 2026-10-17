package prog2.policia_frontend.clients;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.VigilanteDTO;
import prog2.policia_frontend.security.SesionUsuario;

/*
 * Se comunica con los endpoints
 * de vigilantes del backend.
 */
@Service
@VaadinSessionScope
public class VigilanteClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    public VigilanteClient(
            @Value("${backend.url}") String backendUrl,
            SesionUsuario sesionUsuario) {

        this.restClient = RestClient.builder()
                .baseUrl(backendUrl)
                .build();

        this.sesionUsuario = sesionUsuario;
    }

    /*
     * Consulta vigilantes utilizando
     * filtros opcionales.
     */
    public List<VigilanteDTO> listar(
            Boolean activo,
            String nombre) {

        List<VigilanteDTO> vigilantes
                = restClient.get()
                        .uri(constructor -> {

                            constructor.path("/vigilantes");

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
                                        <List<VigilanteDTO>>() {
                                }
                        );

        return vigilantes != null
                ? vigilantes
                : List.of();
    }

    /*
     * Registra un vigilante nuevo.
     */
    public VigilanteDTO crear(
            VigilanteDTO datos) {

        validarDatos(datos, true);

        return restClient.post()
                .uri("/vigilantes")
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .body(VigilanteDTO.class);
    }

    /*
     * Modifica los datos de un vigilante activo.
     */
    public VigilanteDTO actualizar(
            String codigo,
            VigilanteDTO datos) {

        validarCodigo(codigo);
        validarDatos(datos, false);

        return restClient.put()
                .uri(
                        "/vigilantes/{codigo}",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .body(VigilanteDTO.class);
    }

    /*
     * Realiza la baja lógica de un vigilante.
     */
    public void darDeBaja(
            String codigo,
            String motivoBaja) {

        validarCodigo(codigo);

        if (motivoBaja == null
                || motivoBaja.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un motivo de baja."
            );
        }

        VigilanteDTO datos = new VigilanteDTO();
        datos.setMotivoBaja(motivoBaja);

        restClient.patch()
                .uri(
                        "/vigilantes/{codigo}/baja",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .toBodilessEntity();
    }

    /*
     * Reactiva un vigilante inactivo,
     * siempre que el backend lo permita.
     */
    public VigilanteDTO reactivar(
            String codigo) {

        validarCodigo(codigo);

        return restClient.patch()
                .uri(
                        "/vigilantes/{codigo}/reactivar",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .retrieve()
                .body(VigilanteDTO.class);
    }

    private void validarCodigo(
            String codigo) {

        if (codigo == null || codigo.isBlank()) {

            throw new IllegalArgumentException(
                    "El código del vigilante es obligatorio."
            );
        }
    }

    /*
     * Comprueba los datos antes de enviarlos.
     * La contraseña es obligatoria al crear,
     * pero opcional al modificar.
     */
    private void validarDatos(
            VigilanteDTO datos,
            boolean esCreacion) {

        if (datos == null) {

            throw new IllegalArgumentException(
                    "Los datos del vigilante son obligatorios."
            );
        }

        if (datos.getNombre() == null
                || datos.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }

        if (datos.getEdad() == null
                || datos.getEdad() < 18
                || datos.getEdad() > 65) {

            throw new IllegalArgumentException(
                    "La edad debe estar entre 18 y 65 años."
            );
        }

        String password = datos.getPassword();

        if (esCreacion
                && (password == null
                || password.isBlank())) {

            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        if (password != null
                && !password.isBlank()
                && (password.length() < 4
                || password.length() > 20)) {

            throw new IllegalArgumentException(
                    "La contraseña debe tener entre 4 y 20 caracteres."
            );
        }
    }
}