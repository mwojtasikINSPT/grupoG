
package prog2.policia_frontend.clients;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.EntidadBancariaDTO;
import prog2.policia_frontend.security.SesionUsuario;

/*
 * Se comunica con los endpoints
 * de entidades bancarias del backend.
 */
@Service
@VaadinSessionScope
public class EntidadBancariaClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    public EntidadBancariaClient(
            @Value("${backend.url}") String backendUrl,
            SesionUsuario sesionUsuario) {

        this.restClient = RestClient.builder()
                .baseUrl(backendUrl)
                .build();

        this.sesionUsuario = sesionUsuario;
    }

    /*
     * Consulta entidades bancarias utilizando
     * filtros opcionales.
     */
    public List<EntidadBancariaDTO> listar(
            Boolean activo,
            String nombre,
            String domicilio) {

        List<EntidadBancariaDTO> entidades
                = restClient.get()
                        .uri(constructor -> {

                            constructor.path(
                                    "/entidades-bancarias"
                            );

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

                            } else if (domicilio != null
                                    && !domicilio.isBlank()) {

                                constructor.queryParam(
                                        "domicilio",
                                        domicilio.trim()
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
                                        <List<EntidadBancariaDTO>>() {
                                }
                        );

        return entidades != null
                ? entidades
                : List.of();
    }

    /*
     * Crea una entidad bancaria.
     */
    public EntidadBancariaDTO crear(
            EntidadBancariaDTO datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos son obligatorios."
            );
        }

        return restClient.post()
                .uri("/entidades-bancarias")
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .body(EntidadBancariaDTO.class);
    }

    /*
     * Modifica una entidad bancaria existente.
     */
    public EntidadBancariaDTO actualizar(
            String codigo,
            EntidadBancariaDTO datos) {

        validarCodigo(codigo);

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos son obligatorios."
            );
        }

        return restClient.put()
                .uri(
                        "/entidades-bancarias/{codigo}",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .body(EntidadBancariaDTO.class);
    }

    /*
     * Realiza la baja lógica.
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

        EntidadBancariaDTO datos
                = new EntidadBancariaDTO();

        datos.setMotivoBaja(motivoBaja);

        restClient.patch()
                .uri(
                        "/entidades-bancarias/{codigo}/baja",
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
     * Reactiva una entidad bancaria inactiva.
     */
    public EntidadBancariaDTO reactivar(
            String codigo) {

        validarCodigo(codigo);

        return restClient.patch()
                .uri(
                        "/entidades-bancarias/{codigo}/reactivar",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .retrieve()
                .body(EntidadBancariaDTO.class);
    }

    /*
     * Evita realizar peticiones sin código.
     */
    private void validarCodigo(
            String codigo) {

        if (codigo == null || codigo.isBlank()) {

            throw new IllegalArgumentException(
                    "El código es obligatorio."
            );
        }
    }
}