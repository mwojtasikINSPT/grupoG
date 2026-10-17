package prog2.policia_frontend.clients;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.JuezDTO;
import prog2.policia_frontend.security.SesionUsuario;

/* Se comunica con los endpoints de jueces. */
@Service
@VaadinSessionScope
public class JuezClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    public JuezClient(
            @Value("${backend.url}") String backendUrl,
            SesionUsuario sesionUsuario) {

        this.restClient = RestClient.builder()
                .baseUrl(backendUrl)
                .build();
        this.sesionUsuario = sesionUsuario;
    }

    public List<JuezDTO> listar(Boolean activo, String nombre) {

        List<JuezDTO> jueces = restClient.get()
                .uri(constructor -> {
                    constructor.path("/jueces");
                    if (activo != null) {
                        constructor.queryParam("activo", activo);
                    }
                    if (nombre != null && !nombre.isBlank()) {
                        constructor.queryParam("nombre", nombre.trim());
                    }
                    return constructor.build();
                })
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(new ParameterizedTypeReference<List<JuezDTO>>() {});

        return jueces != null ? jueces : List.of();
    }

    public JuezDTO crear(JuezDTO datos) {
        validarDatos(datos);
        return restClient.post()
                .uri("/jueces")
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(JuezDTO.class);
    }

    public JuezDTO actualizar(String codigo, JuezDTO datos) {
        validarCodigo(codigo);
        validarDatos(datos);
        return restClient.put()
                .uri("/jueces/{codigo}", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(JuezDTO.class);
    }

    public void darDeBaja(String codigo, String motivo) {
        validarCodigo(codigo);
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un motivo de baja.");
        }
        JuezDTO datos = new JuezDTO();
        datos.setMotivoBaja(motivo);
        restClient.patch()
                .uri("/jueces/{codigo}/baja", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .toBodilessEntity();
    }

    public JuezDTO reactivar(String codigo) {
        validarCodigo(codigo);
        return restClient.patch()
                .uri("/jueces/{codigo}/reactivar", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(JuezDTO.class);
    }

    private void validarDatos(JuezDTO datos) {
        if (datos == null || datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (datos.getJuezDesde() == null || datos.getJuezDesde().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de inicio debe ser de hoy o pasada.");
        }
    }

    private void validarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del juez es obligatorio.");
        }
    }
}
