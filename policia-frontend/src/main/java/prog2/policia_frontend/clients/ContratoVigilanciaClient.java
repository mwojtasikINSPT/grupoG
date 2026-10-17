package prog2.policia_frontend.clients;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.ContratoVigilanciaDTO;
import prog2.policia_frontend.security.SesionUsuario;

/*
 * Se comunica con los endpoints
 * de contratos de vigilancia.
 */
@Service
@VaadinSessionScope
public class ContratoVigilanciaClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    public ContratoVigilanciaClient(
            @Value("${backend.url}") String backendUrl,
            SesionUsuario sesionUsuario) {

        this.restClient = RestClient.builder()
                .baseUrl(backendUrl)
                .build();

        this.sesionUsuario = sesionUsuario;
    }

    /*
     * Consulta contratos usando como filtros opcionales
     * el estado y el nombre del vigilante.
     */
    public List<ContratoVigilanciaDTO> listar(
            Boolean activo,
            String vigilante) {

        List<ContratoVigilanciaDTO> contratos
                = restClient.get()
                        .uri(constructor -> {

                            constructor.path(
                                    "/contratos-vigilancia"
                            );

                            if (activo != null) {

                                constructor.queryParam(
                                        "activo",
                                        activo
                                );
                            }

                            if (vigilante != null
                                    && !vigilante.isBlank()) {

                                constructor.queryParam(
                                        "vigilante",
                                        vigilante.trim()
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
                                        <List<ContratoVigilanciaDTO>>() {
                                }
                        );

        return contratos != null
                ? contratos
                : List.of();
    }

    /*
     * Consulta los contratos de un vigilante.
     */
    public List<ContratoVigilanciaDTO> listarPorVigilante(
            String codigoVigilante) {

        validarCodigo(codigoVigilante, "vigilante");

        List<ContratoVigilanciaDTO> contratos
                = restClient.get()
                        .uri(
                                "/contratos-vigilancia/vigilante/{codigo}",
                                codigoVigilante.trim().toUpperCase()
                        )
                        .headers(
                                sesionUsuario::aplicarAutenticacion
                        )
                        .retrieve()
                        .body(
                                new ParameterizedTypeReference
                                        <List<ContratoVigilanciaDTO>>() {
                                }
                        );

        return contratos != null
                ? contratos
                : List.of();
    }

    /*
     * Consulta los contratos de una sucursal.
     */
    public List<ContratoVigilanciaDTO> listarPorSucursal(
            String codigoSucursal) {

        validarCodigo(codigoSucursal, "sucursal");

        List<ContratoVigilanciaDTO> contratos
                = restClient.get()
                        .uri(
                                "/contratos-vigilancia/sucursal/{codigo}",
                                codigoSucursal.trim().toUpperCase()
                        )
                        .headers(
                                sesionUsuario::aplicarAutenticacion
                        )
                        .retrieve()
                        .body(
                                new ParameterizedTypeReference
                                        <List<ContratoVigilanciaDTO>>() {
                                }
                        );

        return contratos != null
                ? contratos
                : List.of();
    }

    /*
     * Registra un contrato nuevo.
     */
    public ContratoVigilanciaDTO crear(
            ContratoVigilanciaDTO datos) {

        validarCreacion(datos);

        return restClient.post()
                .uri("/contratos-vigilancia")
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .body(ContratoVigilanciaDTO.class);
    }

    /*
     * Modifica un contrato activo que todavía
     * no pertenece a una fecha pasada.
     */
    public ContratoVigilanciaDTO actualizar(
            String codigo,
            ContratoVigilanciaDTO datos) {

        validarCodigo(codigo, "contrato");
        validarActualizacion(datos);

        return restClient.put()
                .uri(
                        "/contratos-vigilancia/{codigo}",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .body(ContratoVigilanciaDTO.class);
    }

    /*
     * Cancela un contrato. El backend asigna
     * automáticamente el motivo CANCELACION.
     */
    public void cancelar(
            String codigo) {

        validarCodigo(codigo, "contrato");

        restClient.patch()
                .uri(
                        "/contratos-vigilancia/{codigo}/baja",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .retrieve()
                .toBodilessEntity();
    }

    private void validarCreacion(
            ContratoVigilanciaDTO datos) {

        if (datos == null) {

            throw new IllegalArgumentException(
                    "Los datos del contrato son obligatorios."
            );
        }

        validarFecha(datos.getFecha());

        if (datos.getConArma() == null) {

            throw new IllegalArgumentException(
                    "Debe indicar si el contrato requiere arma."
            );
        }

        validarCodigo(
                datos.getVigilanteCodigo(),
                "vigilante"
        );

        validarCodigo(
                datos.getSucursalCodigo(),
                "sucursal"
        );
    }

    private void validarActualizacion(
            ContratoVigilanciaDTO datos) {

        if (datos == null) {

            throw new IllegalArgumentException(
                    "Los datos del contrato son obligatorios."
            );
        }

        if (datos.getFecha() != null) {
            validarFecha(datos.getFecha());
        }
    }

    private void validarFecha(
            LocalDate fecha) {

        if (fecha == null) {

            throw new IllegalArgumentException(
                    "La fecha es obligatoria."
            );
        }

        if (fecha.isBefore(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "La fecha debe ser de hoy o futura."
            );
        }
    }

    private void validarCodigo(
            String codigo,
            String tipo) {

        if (codigo == null || codigo.isBlank()) {

            throw new IllegalArgumentException(
                    "El código del " + tipo + " es obligatorio."
            );
        }
    }
}
