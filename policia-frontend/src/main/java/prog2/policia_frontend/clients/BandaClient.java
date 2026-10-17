package prog2.policia_frontend.clients;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.BandaDTO;
import prog2.policia_frontend.security.SesionUsuario;

/*
 * Se comunica con los endpoints
 * de bandas del backend.
 */
@Service
@VaadinSessionScope
public class BandaClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    public BandaClient(
            @Value("${backend.url}") String backendUrl,
            SesionUsuario sesionUsuario) {

        this.restClient = RestClient.builder()
                .baseUrl(backendUrl)
                .build();

        this.sesionUsuario = sesionUsuario;
    }

    public List<BandaDTO> listar(
            Boolean activo) {

        List<BandaDTO> bandas
                = restClient.get()
                        .uri(constructor -> {

                            constructor.path("/bandas");

                            if (activo != null) {
                                constructor.queryParam("activo", activo);
                            }

                            return constructor.build();
                        })
                        .headers(
                                sesionUsuario::aplicarAutenticacion
                        )
                        .retrieve()
                        .body(
                                new ParameterizedTypeReference
                                        <List<BandaDTO>>() {
                                }
                        );

        return bandas != null
                ? bandas
                : List.of();
    }

    /*
     * El backend crea una banda sin solicitar datos
     * y genera su código automáticamente.
     */
    public BandaDTO crear() {

        return restClient.post()
                .uri("/bandas")
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .retrieve()
                .body(BandaDTO.class);
    }

    public void darDeBaja(
            String codigo) {

        validarCodigo(codigo);

        restClient.patch()
                .uri(
                        "/bandas/{codigo}/baja",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .retrieve()
                .toBodilessEntity();
    }

    public BandaDTO reactivar(
            String codigo) {

        validarCodigo(codigo);

        return restClient.patch()
                .uri(
                        "/bandas/{codigo}/reactivar",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .retrieve()
                .body(BandaDTO.class);
    }

    private void validarCodigo(
            String codigo) {

        if (codigo == null || codigo.isBlank()) {

            throw new IllegalArgumentException(
                    "El código de la banda es obligatorio."
            );
        }
    }
}
