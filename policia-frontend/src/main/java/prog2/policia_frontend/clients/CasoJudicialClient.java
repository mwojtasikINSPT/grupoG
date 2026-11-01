package prog2.policia_frontend.clients;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.CasoJudicialDTO;
import prog2.policia_frontend.dtos.SentenciaAsaltanteDTO;
import prog2.policia_frontend.security.SesionUsuario;

/* Se comunica con los endpoints de casos judiciales. */
@Service
@VaadinSessionScope
public class CasoJudicialClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    public CasoJudicialClient(
            @Value("${backend.url}") String backendUrl,
            SesionUsuario sesionUsuario) {

        this.restClient = RestClient.builder()
                .baseUrl(backendUrl)
                .build();
        this.sesionUsuario = sesionUsuario;
    }

    public List<CasoJudicialDTO> listar(Boolean activo) {

        List<CasoJudicialDTO> casos = restClient.get()
                .uri(constructor -> {
                    constructor.path("/casos-judiciales");
                    if (activo != null) {
                        constructor.queryParam("activo", activo);
                    }
                    return constructor.build();
                })
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CasoJudicialDTO>>() {});

        return casos != null ? casos : List.of();
    }

    public List<CasoJudicialDTO> listarPorAsaltante(String codigo) {

        validarCodigo(codigo, "asaltante");

        List<CasoJudicialDTO> casos = restClient.get()
                .uri(
                        "/casos-judiciales/asaltante/{codigo}",
                        codigo.trim().toUpperCase()
                )
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CasoJudicialDTO>>() {});

        return casos != null ? casos : List.of();
    }

    public List<CasoJudicialDTO> listarPorJuez(String codigo) {

        validarCodigo(codigo, "juez");

        List<CasoJudicialDTO> casos = restClient.get()
                .uri(
                        "/casos-judiciales/juez/{codigo}",
                        codigo.trim().toUpperCase()
                )
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CasoJudicialDTO>>() {});

        return casos != null ? casos : List.of();
    }

    public CasoJudicialDTO buscarPorCodigo(String codigo) {

        validarCodigo(codigo, "caso judicial");

        return restClient.get()
                .uri(
                        "/casos-judiciales/{codigo}",
                        codigo.trim().toUpperCase()
                )
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(CasoJudicialDTO.class);
    }

    public CasoJudicialDTO crear(CasoJudicialDTO datos) {

        validarCreacion(datos);

        return restClient.post()
                .uri("/casos-judiciales")
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(CasoJudicialDTO.class);
    }

    public CasoJudicialDTO actualizar(
            String codigo,
            CasoJudicialDTO datos) {

        validarCodigo(codigo, "caso judicial");

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Debe ingresar algún dato para modificar."
            );
        }

        return restClient.put()
                .uri(
                        "/casos-judiciales/{codigo}",
                        codigo.trim().toUpperCase()
                )
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(CasoJudicialDTO.class);
    }

    public void cerrar(String codigo) {

        validarCodigo(codigo, "caso judicial");

        restClient.patch()
                .uri(
                        "/casos-judiciales/{codigo}/eliminar",
                        codigo.trim().toUpperCase()
                )
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .toBodilessEntity();
    }

    public CasoJudicialDTO reactivar(String codigo) {

        validarCodigo(codigo, "caso judicial");

        return restClient.patch()
                .uri(
                        "/casos-judiciales/{codigo}/reactivar",
                        codigo.trim().toUpperCase()
                )
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(CasoJudicialDTO.class);
    }

    public List<SentenciaAsaltanteDTO> listarSentencias(
            String casoCodigo) {

        validarCodigo(casoCodigo, "caso judicial");

        List<SentenciaAsaltanteDTO> sentencias = restClient.get()
                .uri(
                        "/casos-judiciales/{codigo}/sentencias",
                        casoCodigo.trim().toUpperCase()
                )
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(new ParameterizedTypeReference
                        <List<SentenciaAsaltanteDTO>>() {
                });

        return sentencias != null ? sentencias : List.of();
    }

    public void registrarSentencia(
            String casoCodigo,
            SentenciaAsaltanteDTO sentencia) {

        validarCodigo(casoCodigo, "caso judicial");
        validarSentencia(sentencia);

        restClient.post()
                .uri(
                        "/casos-judiciales/{codigo}/sentencias",
                        casoCodigo.trim().toUpperCase()
                )
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(sentencia)
                .retrieve()
                .toBodilessEntity();
    }

    public void modificarSentencia(
            String casoCodigo,
            SentenciaAsaltanteDTO sentencia) {

        validarCodigo(casoCodigo, "caso judicial");
        validarSentencia(sentencia);

        restClient.put()
                .uri(
                        "/casos-judiciales/{codigo}/sentencias/{asaltante}",
                        casoCodigo.trim().toUpperCase(),
                        sentencia.getAsaltanteCodigo().trim().toUpperCase()
                )
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(sentencia)
                .retrieve()
                .toBodilessEntity();
    }

    private void validarCreacion(CasoJudicialDTO datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del caso son obligatorios."
            );
        }

        if (datos.getAsaltoCodigo() == null
                || datos.getAsaltoCodigo().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un asalto."
            );
        }

        if (datos.getJuezCodigo() == null
                || datos.getJuezCodigo().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un juez."
            );
        }
    }

    private void validarSentencia(SentenciaAsaltanteDTO sentencia) {

        if (sentencia == null
                || sentencia.getAsaltanteCodigo() == null
                || sentencia.getAsaltanteCodigo().isBlank()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un asaltante."
            );
        }

        if (sentencia.getCondenado() == null
                || sentencia.getTiempoCarcel() == null
                || sentencia.getTiempoCarcel() < 0) {
            throw new IllegalArgumentException(
                    "Los datos de la sentencia no son válidos."
            );
        }
    }

    private void validarCodigo(String codigo, String tipo) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del " + tipo + " es obligatorio."
            );
        }
    }
}
