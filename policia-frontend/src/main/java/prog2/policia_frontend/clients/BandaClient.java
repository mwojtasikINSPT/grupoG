package prog2.policia_frontend.clients;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.AsaltanteDTO;
import prog2.policia_frontend.dtos.BandaDTO;
import prog2.policia_frontend.security.SesionUsuario;

// Se comunica con los endpoints de bandas del backend.
@Service
@VaadinSessionScope
public class BandaClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    public BandaClient(@Value("${backend.url}") String backendUrl, SesionUsuario sesionUsuario) {
        this.restClient = RestClient.builder().baseUrl(backendUrl).build();
        this.sesionUsuario = sesionUsuario;
    }

    // Consulta las bandas, opcionalmente filtradas por estado.
    public List<BandaDTO> listar(Boolean activo) {
        List<BandaDTO> bandas = restClient.get()
                .uri(constructor -> {
                    constructor.path("/bandas");
                    if (activo != null) {
                        constructor.queryParam("activo", activo);
                    }
                    return constructor.build();
                })
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(new ParameterizedTypeReference<List<BandaDTO>>() {
                });

        return bandas != null ? bandas : List.of();
    }

    // Crea una banda sin datos; el backend genera su código automáticamente.
    public BandaDTO crear() {
        return restClient.post()
                .uri("/bandas")
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(BandaDTO.class);
    }

    // Consulta los asaltantes integrantes de una banda.
    public List<AsaltanteDTO> listarIntegrantes(String codigo) {
        validarCodigo(codigo);

        List<AsaltanteDTO> integrantes = restClient.get()
                .uri("/bandas/{codigo}/integrantes", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(new ParameterizedTypeReference<List<AsaltanteDTO>>() {
                });

        return integrantes != null ? integrantes : List.of();
    }

    // Realiza la baja lógica de una banda.
    public void darDeBaja(String codigo) {
        validarCodigo(codigo);

        restClient.patch()
                .uri("/bandas/{codigo}/baja", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .toBodilessEntity();
    }

    // Reactiva una banda inactiva.
    public BandaDTO reactivar(String codigo) {
        validarCodigo(codigo);

        return restClient.patch()
                .uri("/bandas/{codigo}/reactivar", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(BandaDTO.class);
    }

    // Evita realizar peticiones sin un código de banda válido.
    private void validarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código de la banda es obligatorio.");
        }
    }
}
