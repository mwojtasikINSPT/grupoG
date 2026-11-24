package prog2.policia_frontend.clients;

import java.util.List;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import prog2.policia_frontend.dtos.SucursalDTO;
import prog2.policia_frontend.security.SesionUsuario;

// Se comunica con los endpoints de sucursales del backend.
@Service
@VaadinSessionScope
public class SucursalClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    public SucursalClient(@Value("${backend.url}") String backendUrl, SesionUsuario sesionUsuario) {
        this.restClient = RestClient.builder().baseUrl(backendUrl).build();
        this.sesionUsuario = sesionUsuario;
    }

    // El estado se conserva para los selectores de otras funcionalidades.
    public List<SucursalDTO> listar(Boolean activo) {
        String ruta = "/sucursales";
        if (activo != null) {
            ruta += "?activo=" + activo;
        }
        List<SucursalDTO> sucursales = restClient.get()
                .uri(ruta)
                .headers(new java.util.function.Consumer<org.springframework.http.HttpHeaders>() {
                    @Override
                    public void accept(org.springframework.http.HttpHeaders headers) {
                        sesionUsuario.aplicarAutenticacion(headers);
                    }
                })
                .retrieve()
                .body(new ParameterizedTypeReference<List<SucursalDTO>>() {});

        return sucursales != null ? sucursales : List.of();
    }

    // Crea una sucursal.
    public SucursalDTO crear(SucursalDTO datos) {
        validarDatos(datos);

        return restClient.post()
                .uri("/sucursales")
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(SucursalDTO.class);
    }

    // Modifica una sucursal activa.
    public SucursalDTO actualizar(String codigo, SucursalDTO datos) {
        validarCodigo(codigo);
        validarDatos(datos);

        return restClient.put()
                .uri("/sucursales/{codigo}", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(SucursalDTO.class);
    }

    // Realiza la baja lógica de una sucursal.
    public void darDeBaja(String codigo, String motivoCierre) {
        validarCodigo(codigo);

        if (motivoCierre == null || motivoCierre.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un motivo de cierre.");
        }

        SucursalDTO datos = new SucursalDTO();
        datos.setMotivoCierre(motivoCierre);

        restClient.patch()
                .uri("/sucursales/{codigo}/baja", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .toBodilessEntity();
    }

    // Reactiva una sucursal que no fue cerrada definitivamente.
    public SucursalDTO reactivar(String codigo) {
        validarCodigo(codigo);

        return restClient.patch()
                .uri("/sucursales/{codigo}/reactivar", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(SucursalDTO.class);
    }

    // Valida que el código sea obligatorio.
    private void validarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código de la sucursal es obligatorio.");
        }
    }

    // Comprueba los datos antes de enviarlos al backend.
    private void validarDatos(SucursalDTO datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos de la sucursal son obligatorios.");
        }

        if (datos.getDomicilio() == null || datos.getDomicilio().isBlank()) {
            throw new IllegalArgumentException("El domicilio es obligatorio.");
        }

        // Exige al menos un empleado; el backend también debe validarlo.
        if (datos.getCantEmpleados() == null || datos.getCantEmpleados() <= 0) {
            throw new IllegalArgumentException("La cantidad de empleados debe ser mayor que cero.");
        }

        if (datos.getEntidadBancariaCodigo() == null || datos.getEntidadBancariaCodigo().isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una entidad bancaria.");
        }
    }

    // Consulta los motivos de cierre disponibles en el backend.
    public List<String> listarMotivosCierre() {
        String[] motivos = restClient.get()
                .uri("/sucursales/motivos-cierre")
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(String[].class);

        return motivos != null ? List.of(motivos) : List.of();
    }
}
