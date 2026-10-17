package prog2.policia_frontend.clients;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.AsaltoDTO;
import prog2.policia_frontend.security.SesionUsuario;

/*
 * Se comunica con los endpoints
 * de asaltos del backend.
 */
@Service
@VaadinSessionScope
public class AsaltoClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    public AsaltoClient(
            @Value("${backend.url}") String backendUrl,
            SesionUsuario sesionUsuario) {

        this.restClient = RestClient.builder()
                .baseUrl(backendUrl)
                .build();

        this.sesionUsuario = sesionUsuario;
    }

    public List<AsaltoDTO> listar() {

        List<AsaltoDTO> asaltos
                = restClient.get()
                        .uri("/asaltos")
                        .headers(
                                sesionUsuario::aplicarAutenticacion
                        )
                        .retrieve()
                        .body(
                                new ParameterizedTypeReference
                                        <List<AsaltoDTO>>() {
                                }
                        );

        return asaltos != null
                ? asaltos
                : List.of();
    }

    public AsaltoDTO crear(
            AsaltoDTO datos) {

        validarCreacion(datos);

        return restClient.post()
                .uri("/asaltos")
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .body(AsaltoDTO.class);
    }

    public AsaltoDTO actualizar(
            String codigo,
            AsaltoDTO datos) {

        validarCodigo(codigo);

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del asalto son obligatorios."
            );
        }

        return restClient.put()
                .uri(
                        "/asaltos/{codigo}",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .body(AsaltoDTO.class);
    }

    public List<AsaltoDTO> listarPorAsaltante(
            String codigoAsaltante) {

        return listarPorRuta(
                "/asaltos/asaltante/{codigo}",
                codigoAsaltante
        );
    }

    public List<AsaltoDTO> listarPorSucursal(
            String codigoSucursal) {

        return listarPorRuta(
                "/asaltos/sucursal/{codigo}",
                codigoSucursal
        );
    }

    private List<AsaltoDTO> listarPorRuta(
            String ruta,
            String codigo) {

        validarCodigo(codigo);

        List<AsaltoDTO> asaltos
                = restClient.get()
                        .uri(
                                ruta,
                                codigo.trim().toUpperCase()
                        )
                        .headers(
                                sesionUsuario::aplicarAutenticacion
                        )
                        .retrieve()
                        .body(
                                new ParameterizedTypeReference
                                        <List<AsaltoDTO>>() {
                                }
                        );

        return asaltos != null
                ? asaltos
                : List.of();
    }

    private void validarCreacion(
            AsaltoDTO datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del asalto son obligatorios."
            );
        }

        if (datos.getFecha() == null
                || datos.getFecha().isAfter(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "La fecha debe ser de hoy o pasada."
            );
        }

        if (datos.getSucursalCodigo() == null
                || datos.getSucursalCodigo().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una sucursal."
            );
        }
    }

    private void validarCodigo(
            String codigo) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                    "El código es obligatorio."
            );
        }
    }
}
