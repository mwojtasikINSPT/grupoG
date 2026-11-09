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

// Se comunica con los endpoints de casos judiciales.
@Service
@VaadinSessionScope
public class CasoJudicialClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    public CasoJudicialClient(@Value("${backend.url}") String backendUrl, SesionUsuario sesionUsuario) {
        this.restClient = RestClient.builder().baseUrl(backendUrl).build();
        this.sesionUsuario = sesionUsuario;
    }

    // Consulta casos judiciales filtrando opcionalmente por estado.
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
                .body(new ParameterizedTypeReference<List<CasoJudicialDTO>>() {
                });

        return casos != null ? casos : List.of();
    }

    // Consulta los casos asociados a un asaltante.
    public List<CasoJudicialDTO> listarPorAsaltante(String codigo) {
        return listarPorRuta("/casos-judiciales/asaltante/{codigo}", codigo, "asaltante");
    }

    // Consulta los casos asignados a un juez.
    public List<CasoJudicialDTO> listarPorJuez(String codigo) {
        return listarPorRuta("/casos-judiciales/juez/{codigo}", codigo, "juez");
    }

    // Ejecuta consultas de casos judiciales por código.
    private List<CasoJudicialDTO> listarPorRuta(String ruta, String codigo, String tipo) {
        validarCodigo(codigo, tipo);

        List<CasoJudicialDTO> casos = restClient.get()
                .uri(ruta, codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CasoJudicialDTO>>() {
                });

        return casos != null ? casos : List.of();
    }

    // Busca un caso judicial por su código.
    public CasoJudicialDTO buscarPorCodigo(String codigo) {
        validarCodigo(codigo, "caso judicial");

        return restClient.get()
                .uri("/casos-judiciales/{codigo}", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(CasoJudicialDTO.class);
    }

    // Registra un nuevo caso judicial.
    public CasoJudicialDTO crear(CasoJudicialDTO datos) {
        validarCreacion(datos);

        return restClient.post()
                .uri("/casos-judiciales")
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(CasoJudicialDTO.class);
    }

    // Modifica los datos de un caso judicial existente.
    public CasoJudicialDTO actualizar(String codigo, CasoJudicialDTO datos) {
        validarCodigo(codigo, "caso judicial");

        if (datos == null) {
            throw new IllegalArgumentException("Debe ingresar algún dato para modificar.");
        }

        return restClient.put()
                .uri("/casos-judiciales/{codigo}", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(CasoJudicialDTO.class);
    }

    // Cierra un caso judicial mediante baja lógica.
    public void cerrar(String codigo) {
        validarCodigo(codigo, "caso judicial");

        restClient.patch()
                .uri("/casos-judiciales/{codigo}/eliminar", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .toBodilessEntity();
    }

    // Reactiva un caso judicial indicando el motivo de apelación.
    public CasoJudicialDTO reactivar(String codigo, String motivo) {
        validarCodigo(codigo, "caso judicial");

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el motivo de la apelación.");
        }

        return restClient.patch()
                .uri(constructor -> constructor.path("/casos-judiciales/{codigo}/reactivar").queryParam("motivo", motivo.trim()).build(codigo.trim().toUpperCase()))
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(CasoJudicialDTO.class);
    }

    // Consulta las sentencias registradas en un caso judicial.
    public List<SentenciaAsaltanteDTO> listarSentencias(String casoCodigo) {
        validarCodigo(casoCodigo, "caso judicial");

        List<SentenciaAsaltanteDTO> sentencias = restClient.get()
                .uri("/casos-judiciales/{codigo}/sentencias", casoCodigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(new ParameterizedTypeReference<List<SentenciaAsaltanteDTO>>() {
                });

        return sentencias != null ? sentencias : List.of();
    }

    // Registra la sentencia de un asaltante en un caso judicial.
    public void registrarSentencia(String casoCodigo, SentenciaAsaltanteDTO sentencia) {
        validarCodigo(casoCodigo, "caso judicial");
        validarSentencia(sentencia);

        restClient.post()
                .uri("/casos-judiciales/{codigo}/sentencias", casoCodigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(sentencia)
                .retrieve()
                .toBodilessEntity();
    }

    // Modifica una sentencia existente de un asaltante.
    public void modificarSentencia(String casoCodigo, SentenciaAsaltanteDTO sentencia) {
        validarCodigo(casoCodigo, "caso judicial");
        validarSentencia(sentencia);

        restClient.put()
                .uri("/casos-judiciales/{codigo}/sentencias/{asaltante}", casoCodigo.trim().toUpperCase(), sentencia.getAsaltanteCodigo().trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(sentencia)
                .retrieve()
                .toBodilessEntity();
    }

    // Verifica los datos obligatorios para crear un caso judicial.
    private void validarCreacion(CasoJudicialDTO datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos del caso son obligatorios.");
        }

        if (datos.getAsaltoCodigo() == null || datos.getAsaltoCodigo().isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un asalto.");
        }

        if (datos.getJuezCodigo() == null || datos.getJuezCodigo().isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un juez.");
        }
    }

    // Verifica el asaltante, la condena y el tiempo de cárcel.
    private void validarSentencia(SentenciaAsaltanteDTO sentencia) {
        if (sentencia == null || sentencia.getAsaltanteCodigo() == null || sentencia.getAsaltanteCodigo().isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un asaltante.");
        }

        if (sentencia.getCondenado() == null || sentencia.getTiempoCarcel() == null || sentencia.getTiempoCarcel() < 0) {
            throw new IllegalArgumentException("Los datos de la sentencia no son válidos.");
        }
    }

    // Evita realizar peticiones sin el código correspondiente.
    private void validarCodigo(String codigo, String tipo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del " + tipo + " es obligatorio.");
        }
    }
}
