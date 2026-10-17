package prog2.policia_frontend.clients;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.InvestigadorDTO;
import prog2.policia_frontend.security.SesionUsuario;

/*
 * Se comunica con los endpoints
 * de investigadores del backend.
 */
@Service
@VaadinSessionScope
public class InvestigadorClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    public InvestigadorClient(
            @Value("${backend.url}") String backendUrl,
            SesionUsuario sesionUsuario) {

        this.restClient = RestClient.builder()
                .baseUrl(backendUrl)
                .build();

        this.sesionUsuario = sesionUsuario;
    }

    /*
     * Consulta investigadores utilizando
     * filtros opcionales.
     */
    public List<InvestigadorDTO> listar(
            Boolean activo,
            String nombre) {

        List<InvestigadorDTO> investigadores
                = restClient.get()
                        .uri(constructor -> {

                            constructor.path("/investigadores");

                            if (activo != null) {
                                constructor.queryParam("activo", activo);
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
                                        <List<InvestigadorDTO>>() {
                                }
                        );

        return investigadores != null
                ? investigadores
                : List.of();
    }

    public InvestigadorDTO crear(
            InvestigadorDTO datos) {

        validarDatos(datos, true);

        return restClient.post()
                .uri("/investigadores")
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .body(InvestigadorDTO.class);
    }

    public InvestigadorDTO actualizar(
            String codigo,
            InvestigadorDTO datos) {

        validarCodigo(codigo);
        validarDatos(datos, false);

        return restClient.put()
                .uri(
                        "/investigadores/{codigo}",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .body(InvestigadorDTO.class);
    }

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

        InvestigadorDTO datos = new InvestigadorDTO();
        datos.setMotivoBaja(motivoBaja);

        restClient.patch()
                .uri(
                        "/investigadores/{codigo}/baja",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .toBodilessEntity();
    }

    public InvestigadorDTO reactivar(
            String codigo) {

        validarCodigo(codigo);

        return restClient.patch()
                .uri(
                        "/investigadores/{codigo}/reactivar",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .retrieve()
                .body(InvestigadorDTO.class);
    }

    private void validarCodigo(
            String codigo) {

        if (codigo == null || codigo.isBlank()) {

            throw new IllegalArgumentException(
                    "El código del investigador es obligatorio."
            );
        }
    }

    private void validarDatos(
            InvestigadorDTO datos,
            boolean esCreacion) {

        if (datos == null) {

            throw new IllegalArgumentException(
                    "Los datos del investigador son obligatorios."
            );
        }

        if (datos.getNombre() == null
                || datos.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
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
