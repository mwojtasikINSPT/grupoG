package prog2.policia_frontend.clients;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.AsaltanteDTO;
import prog2.policia_frontend.dtos.AbogadoDisponibleDTO;
import prog2.policia_frontend.dtos.AsignacionAbogadoDTO;
import prog2.policia_frontend.dtos.ResultadoAsignacionAbogadoDTO;
import prog2.policia_frontend.security.SesionUsuario;

/*
 * Se comunica con los endpoints
 * de asaltantes del backend.
 */
@Service
@VaadinSessionScope
public class AsaltanteClient {
    private final java.util.function.Consumer<org.springframework.http.HttpHeaders> autenticacion;

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    public AsaltanteClient(
            @Value("${backend.url}") String backendUrl,
            SesionUsuario sesionUsuario) {

        this.restClient = RestClient.builder()
                .baseUrl(backendUrl)
                .build();

        this.sesionUsuario = sesionUsuario;
        this.autenticacion = new java.util.function.Consumer<org.springframework.http.HttpHeaders>() {
            @Override
            public void accept(org.springframework.http.HttpHeaders headers) { sesionUsuario.aplicarAutenticacion(headers); }
        };
    }

    public List<AbogadoDisponibleDTO> listarAbogadosDisponibles() {
        List<AbogadoDisponibleDTO> resultado = restClient.get().uri("/usuarios/abogados-disponibles")
                .headers(autenticacion).retrieve().body(new ParameterizedTypeReference<List<AbogadoDisponibleDTO>>() {});
        return resultado == null ? List.of() : resultado;
    }

    public ResultadoAsignacionAbogadoDTO asignarAbogado(String asaltante, String abogado) {
        AsignacionAbogadoDTO datos = new AsignacionAbogadoDTO();
        datos.setAbogadoCodigo(abogado);
        return restClient.put().uri("/asaltantes/{codigo}/abogado-defensor", asaltante)
                .headers(autenticacion).body(datos).retrieve().body(ResultadoAsignacionAbogadoDTO.class);
    }

    public ResultadoAsignacionAbogadoDTO desvincularAbogado(String asaltante) {
        return restClient.delete().uri("/asaltantes/{codigo}/abogado-defensor", asaltante)
                .headers(autenticacion).retrieve().body(ResultadoAsignacionAbogadoDTO.class);
    }

    public List<AsaltanteDTO> listar(
            Boolean activo,
            String nombre) {

        List<AsaltanteDTO> asaltantes
                = restClient.get()
                        .uri(constructor -> {

                            constructor.path("/asaltantes");

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
                                        <List<AsaltanteDTO>>() {
                                }
                        );

        return asaltantes != null
                ? asaltantes
                : List.of();
    }

    public AsaltanteDTO crear(
            AsaltanteDTO datos) {

        validarNombre(datos);

        return restClient.post()
                .uri("/asaltantes")
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .body(AsaltanteDTO.class);
    }

    public AsaltanteDTO actualizar(
            String codigo,
            AsaltanteDTO datos) {

        validarCodigo(codigo);
        validarNombre(datos);

        return restClient.put()
                .uri(
                        "/asaltantes/{codigo}",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .body(AsaltanteDTO.class);
    }

    /*
     * La única baja permitida por el backend
     * para un asaltante es FALLECIMIENTO.
     */
    public void darDeBaja(
            String codigo) {

        validarCodigo(codigo);

        AsaltanteDTO datos = new AsaltanteDTO();
        datos.setMotivoBaja("FALLECIMIENTO");

        restClient.patch()
                .uri(
                        "/asaltantes/{codigo}/baja",
                        codigo.trim().toUpperCase()
                )
                .headers(
                        sesionUsuario::aplicarAutenticacion
                )
                .body(datos)
                .retrieve()
                .toBodilessEntity();
    }

    private void validarNombre(
            AsaltanteDTO datos) {

        if (datos == null
                || datos.getNombre() == null
                || datos.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre del asaltante es obligatorio."
            );
        }
    }

    private void validarCodigo(
            String codigo) {

        if (codigo == null || codigo.isBlank()) {

            throw new IllegalArgumentException(
                    "El código del asaltante es obligatorio."
            );
        }
    }
}
