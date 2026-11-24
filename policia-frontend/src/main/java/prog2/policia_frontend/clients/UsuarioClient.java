package prog2.policia_frontend.clients;

import java.util.List;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import prog2.policia_frontend.dtos.BajaUsuarioDTO;
import prog2.policia_frontend.dtos.ResultadoBajaUsuarioDTO;
import prog2.policia_frontend.dtos.CambioPasswordDTO;
import prog2.policia_frontend.dtos.UsuarioDTO;
import prog2.policia_frontend.dtos.UsuarioFormularioDTO;
import prog2.policia_frontend.security.SesionUsuario;

// Se comunica con los endpoints de usuarios del backend.
@Service
@VaadinSessionScope
public class UsuarioClient {

    private final RestClient restClient;
    private final SesionUsuario sesionUsuario;

    // Recibe la dirección del backend y la sesión del usuario conectado.
    public UsuarioClient(@Value("${backend.url}") String backendUrl, SesionUsuario sesionUsuario) {
        this.restClient = RestClient.builder().baseUrl(backendUrl).build();
        this.sesionUsuario = sesionUsuario;
    }

    // Consulta los usuarios autorizados; el estado se conserva para formularios.
    public List<UsuarioDTO> listar(Boolean activo) {
        String ruta = "/usuarios";
        if (activo != null) { ruta += "?activo=" + activo; }
        List<UsuarioDTO> usuarios = restClient.get().uri(ruta)
                .headers(new java.util.function.Consumer<org.springframework.http.HttpHeaders>() {
                    @Override public void accept(org.springframework.http.HttpHeaders headers) { sesionUsuario.aplicarAutenticacion(headers); }
                }).retrieve().body(new ParameterizedTypeReference<List<UsuarioDTO>>() {});
        return usuarios == null ? List.of() : usuarios;
    }

    // Realiza la baja lógica del usuario, conservándolo como inactivo.
    public ResultadoBajaUsuarioDTO darDeBaja(String codigo, String motivoBaja) {
        validarCodigo(codigo);

        if (motivoBaja == null || motivoBaja.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un motivo de baja.");
        }

        BajaUsuarioDTO datosBaja = new BajaUsuarioDTO(motivoBaja);

        return restClient.patch()
                .uri("/usuarios/{codigo}/baja", codigo.trim().toUpperCase())
                .headers(new java.util.function.Consumer<org.springframework.http.HttpHeaders>() {
                    @Override
                    public void accept(org.springframework.http.HttpHeaders headers) { sesionUsuario.aplicarAutenticacion(headers); }
                })
                .body(datosBaja)
                .retrieve()
                .body(ResultadoBajaUsuarioDTO.class);
    }

    // Solicita la reactivación de un usuario inactivo.
    public UsuarioDTO reactivar(String codigo) {
        validarCodigo(codigo);

        return restClient.patch()
                .uri("/usuarios/{codigo}/reactivar", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(UsuarioDTO.class);
    }

    // Envía los datos necesarios para crear un usuario.
    public UsuarioDTO crear(UsuarioFormularioDTO datos) {
        validarDatos(datos);

        return restClient.post()
                .uri("/usuarios")
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(UsuarioDTO.class);
    }

    // Modifica un usuario existente sin cambiar su contraseña si no se proporciona otra.
    public UsuarioDTO actualizar(String codigo, UsuarioFormularioDTO datos) {
        validarCodigo(codigo);
        validarDatos(datos);

        return restClient.put()
                .uri("/usuarios/{codigo}", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .body(UsuarioDTO.class);
    }

    // Cambia la contraseña del usuario autenticado.
    public void cambiarPasswordPropia(String passwordActual, String passwordNueva) {
        if (passwordActual == null || passwordActual.isBlank()) {
            throw new IllegalArgumentException("La contraseña actual es obligatoria.");
        }

        if (passwordNueva == null || passwordNueva.isBlank()) {
            throw new IllegalArgumentException("La nueva contraseña es obligatoria.");
        }

        CambioPasswordDTO datos = new CambioPasswordDTO(passwordActual, passwordNueva);

        restClient.patch()
                .uri("/usuarios/password")
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datos)
                .retrieve()
                .toBodilessEntity();
    }

    // Consulta los motivos de baja disponibles en el backend.
    public List<String> listarMotivosBaja() {
        String[] motivos = restClient.get()
                .uri("/usuarios/motivos-baja")
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(String[].class);

        return motivos != null ? List.of(motivos) : List.of();
    }

    // Valida que el código del usuario sea obligatorio.
    private void validarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del usuario es obligatorio.");
        }
    }

    // Valida que se reciban los datos del usuario.
    private void validarDatos(UsuarioFormularioDTO datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Los datos del usuario son obligatorios.");
        }
    }
}
