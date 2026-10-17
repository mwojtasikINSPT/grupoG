
package prog2.policia_frontend.clients;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.SucursalDTO;
import prog2.policia_frontend.security.SesionUsuario;

/*
 * Se comunica con los endpoints
 * de sucursales del backend.
 */
@Service
@VaadinSessionScope
public class SucursalClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    public SucursalClient(
            @Value("${backend.url}") String backendUrl,
            SesionUsuario sesionUsuario) {

        this.restClient = RestClient.builder()
                .baseUrl(backendUrl)
                .build();

        this.sesionUsuario = sesionUsuario;
    }

    /*
     * Consulta sucursales con filtros opcionales.
     */
    public List<SucursalDTO> listar(
            Boolean activo,
            String domicilio) {

        List<SucursalDTO> sucursales
                = restClient.get()
                        .uri(constructor -> {

                            constructor.path("/sucursales");

                            if (activo != null) {

                                constructor.queryParam(
                                        "activo",
                                        activo
                                );
                            }

                            if (domicilio != null
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
                                        <List<SucursalDTO>>() {
                                }
                        );

        return sucursales != null
                ? sucursales
                : List.of();
    }

    /*
     * Consulta todas las sucursales relacionadas
     * con una entidad bancaria determinada.
     */
    public List<SucursalDTO> listarPorEntidadBancaria(
            String codigoEntidad) {

        validarCodigo(codigoEntidad);

        List<SucursalDTO> sucursales
                = restClient.get()
                        .uri(
                                "/entidades-bancarias/{codigo}/sucursales",
                                codigoEntidad.trim().toUpperCase()
                        )
                        .headers(
                                sesionUsuario::aplicarAutenticacion
                        )
                        .retrieve()
                        .body(
                                new ParameterizedTypeReference
                                        <List<SucursalDTO>>() {
                                }
                        );

        return sucursales != null
                ? sucursales
                : List.of();
    }

    /*
     * Crea una sucursal.
     */
    public SucursalDTO crear(
            SucursalDTO datos) {

        validarDatos(datos);

        return restClient.post()
                .uri("/sucursales")
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .body(SucursalDTO.class);
    }

    /*
     * Modifica una sucursal activa.
     */
    public SucursalDTO actualizar(
            String codigo,
            SucursalDTO datos) {

        validarCodigo(codigo);
        validarDatos(datos);

        return restClient.put()
                .uri(
                        "/sucursales/{codigo}",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .body(SucursalDTO.class);
    }

    /*
     * Realiza la baja lógica de una sucursal.
     */
    public void darDeBaja(
            String codigo,
            String motivoCierre) {

        validarCodigo(codigo);

        if (motivoCierre == null
                || motivoCierre.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un motivo de cierre."
            );
        }

        SucursalDTO datos = new SucursalDTO();
        datos.setMotivoCierre(motivoCierre);

        restClient.patch()
                .uri(
                        "/sucursales/{codigo}/baja",
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
     * Reactiva una sucursal que no fue
     * cerrada definitivamente.
     */
    public SucursalDTO reactivar(
            String codigo) {

        validarCodigo(codigo);

        return restClient.patch()
                .uri(
                        "/sucursales/{codigo}/reactivar",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .retrieve()
                .body(SucursalDTO.class);
    }

    private void validarCodigo(
            String codigo) {

        if (codigo == null || codigo.isBlank()) {

            throw new IllegalArgumentException(
                    "El código de la sucursal es obligatorio."
            );
        }
    }

    /*
     * Realiza comprobaciones básicas antes
     * de enviar la información.
     */
    private void validarDatos(
            SucursalDTO datos) {

        if (datos == null) {

            throw new IllegalArgumentException(
                    "Los datos de la sucursal son obligatorios."
            );
        }

        if (datos.getDomicilio() == null
                || datos.getDomicilio().isBlank()) {

            throw new IllegalArgumentException(
                    "El domicilio es obligatorio."
            );
        }

        /*
         * En el frontend exigimos al menos un empleado.
         * El backend también debería aplicar esta regla.
         */
        if (datos.getCantEmpleados() == null
                || datos.getCantEmpleados() <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad de empleados debe ser mayor que cero."
            );
        }

        if (datos.getEntidadBancariaCodigo() == null
                || datos.getEntidadBancariaCodigo().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una entidad bancaria."
            );
        }
    }
}
