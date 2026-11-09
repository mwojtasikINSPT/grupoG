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

// Se comunica con los endpoints de asaltos del backend.
@Service
@VaadinSessionScope
public class AsaltoClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    public AsaltoClient(@Value("${backend.url}") String backendUrl, SesionUsuario sesionUsuario) {
        this.restClient = RestClient.builder().baseUrl(backendUrl).build();
        this.sesionUsuario = sesionUsuario;
    }

    // Consulta todos los asaltos registrados.
    public List<AsaltoDTO> listar() {
        List<AsaltoDTO> asaltos = restClient.get()
                .uri("/asaltos")
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(new ParameterizedTypeReference<List<AsaltoDTO>>() {});

        return asaltos != null ? asaltos : List.of();
    }

    // Registra un nuevo asalto.
    public AsaltoDTO crear(AsaltoDTO datos) {
        validarCreacion(datos);

        return restClient.post()
                .uri("/asaltos")
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(AsaltoDTO.class);
    }

    // Modifica un asalto existente.
    public AsaltoDTO actualizar(String codigo, AsaltoDTO datos) {
        validarCodigo(codigo);
        validarDatos(datos);

        return restClient.put()
                .uri("/asaltos/{codigo}", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(AsaltoDTO.class);
    }

    // Consulta los asaltos asociados a un asaltante.
    public List<AsaltoDTO> listarPorAsaltante(String codigoAsaltante) {
        return listarPorRuta("/asaltos/asaltante/{codigo}", codigoAsaltante);
    }

    // Consulta los asaltos registrados en una sucursal.
    public List<AsaltoDTO> listarPorSucursal(String codigoSucursal) {
        return listarPorRuta("/asaltos/sucursal/{codigo}", codigoSucursal);
    }

    // Ejecuta consultas de asaltos utilizando una ruta y un código.
    private List<AsaltoDTO> listarPorRuta(String ruta, String codigo) {
        validarCodigo(codigo);

        List<AsaltoDTO> asaltos = restClient.get()
                .uri(ruta, codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(new ParameterizedTypeReference<List<AsaltoDTO>>() {});

        return asaltos != null ? asaltos : List.of();
    }

    // Valida los datos necesarios para registrar un asalto.
    private void validarCreacion(AsaltoDTO datos) {
        validarDatos(datos);

        if (datos.getFecha() == null || datos.getFecha().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha debe ser de hoy o pasada.");
        }

        if (datos.getSucursalCodigo() == null || datos.getSucursalCodigo().isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una sucursal.");
        }
    }

    // Verifica que se reciban los datos del asalto.
    private void validarDatos(AsaltoDTO datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos del asalto son obligatorios.");
        }
    }

    // Evita realizar peticiones sin código.
    private void validarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio.");
        }
    }
}