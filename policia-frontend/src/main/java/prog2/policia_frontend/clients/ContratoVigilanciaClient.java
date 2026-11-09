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

// Se comunica con los endpoints de contratos de vigilancia.
@Service
@VaadinSessionScope
public class ContratoVigilanciaClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    public ContratoVigilanciaClient(@Value("${backend.url}") String backendUrl, SesionUsuario sesionUsuario) {
        this.restClient = RestClient.builder().baseUrl(backendUrl).build();
        this.sesionUsuario = sesionUsuario;
    }

    // Consulta contratos filtrando opcionalmente por estado y vigilante.
    public List<ContratoVigilanciaDTO> listar(Boolean activo, String vigilante) {
        List<ContratoVigilanciaDTO> contratos = restClient.get()
                .uri(constructor -> {
                    constructor.path("/contratos-vigilancia");
                    if (activo != null) {
                        constructor.queryParam("activo", activo);
                    }
                    if (vigilante != null && !vigilante.isBlank()) {
                        constructor.queryParam("vigilante", vigilante.trim());
                    }
                    return constructor.build();
                })
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ContratoVigilanciaDTO>>() {
                });

        return contratos != null ? contratos : List.of();
    }

    // Consulta los contratos asociados a un vigilante.
    public List<ContratoVigilanciaDTO> listarPorVigilante(String codigoVigilante) {
        return listarPorRuta("/contratos-vigilancia/vigilante/{codigo}", codigoVigilante, "vigilante");
    }

    // Consulta los contratos asociados a una sucursal.
    public List<ContratoVigilanciaDTO> listarPorSucursal(String codigoSucursal) {
        return listarPorRuta("/contratos-vigilancia/sucursal/{codigo}", codigoSucursal, "sucursal");
    }

    // Ejecuta consultas de contratos mediante una ruta y un código.
    private List<ContratoVigilanciaDTO> listarPorRuta(String ruta, String codigo, String tipo) {
        validarCodigo(codigo, tipo);

        List<ContratoVigilanciaDTO> contratos = restClient.get()
                .uri(ruta, codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ContratoVigilanciaDTO>>() {
                });

        return contratos != null ? contratos : List.of();
    }

    // Registra un contrato nuevo.
    public ContratoVigilanciaDTO crear(ContratoVigilanciaDTO datos) {
        validarCreacion(datos);

        return restClient.post()
                .uri("/contratos-vigilancia")
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(ContratoVigilanciaDTO.class);
    }

    // Modifica un contrato existente respetando las validaciones de fecha.
    public ContratoVigilanciaDTO actualizar(String codigo, ContratoVigilanciaDTO datos) {
        validarCodigo(codigo, "contrato");
        validarActualizacion(datos);

        return restClient.put()
                .uri("/contratos-vigilancia/{codigo}", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(ContratoVigilanciaDTO.class);
    }

    // Cancela un contrato; el backend asigna automáticamente el motivo CANCELACION.
    public void cancelar(String codigo) {
        validarCodigo(codigo, "contrato");

        restClient.patch()
                .uri("/contratos-vigilancia/{codigo}/baja", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .toBodilessEntity();
    }

    // Verifica los datos obligatorios para crear un contrato.
    private void validarCreacion(ContratoVigilanciaDTO datos) {
        validarDatos(datos);
        validarFecha(datos.getFecha());

        if (datos.getConArma() == null) {
            throw new IllegalArgumentException("Debe indicar si el contrato requiere arma.");
        }

        validarCodigo(datos.getVigilanteCodigo(), "vigilante");
        validarCodigo(datos.getSucursalCodigo(), "sucursal");
    }

    // Valida los datos enviados para modificar un contrato.
    private void validarActualizacion(ContratoVigilanciaDTO datos) {
        validarDatos(datos);
        if (datos.getFecha() != null) {
            validarFecha(datos.getFecha());
        }
    }

    // Verifica que se reciban los datos del contrato.
    private void validarDatos(ContratoVigilanciaDTO datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos del contrato son obligatorios.");
        }
    }

    // Verifica que la fecha sea de hoy o futura.
    private void validarFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }

        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha debe ser de hoy o futura.");
        }
    }

    // Evita realizar peticiones sin el código correspondiente.
    private void validarCodigo(String codigo, String tipo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del " + tipo + " es obligatorio.");
        }
    }
}
