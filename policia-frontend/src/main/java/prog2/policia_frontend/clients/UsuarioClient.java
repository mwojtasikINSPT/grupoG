package prog2.policia_frontend.clients;

import java.util.List;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import prog2.policia_frontend.dtos.BajaUsuarioDTO;
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

    // Consulta usuarios por estado y coincidencias de nombre.
    // activo: null = todos, true = activos, false = inactivos.
    // nombre: null o vacío = sin filtro; con contenido = busca coincidencias.
    public List<UsuarioDTO> listar(Boolean activo, String nombre) {
        List<UsuarioDTO> usuarios = restClient.get()
                .uri(constructor -> {
                    constructor.path("/usuarios");
                    if (activo != null) constructor.queryParam("activo", activo);
                    if (nombre != null && !nombre.isBlank()) constructor.queryParam("nombre", nombre.trim());
                    return constructor.build();
                })
                .headers(sesionUsuario::aplicarAutenticacion)
                .retrieve()
                .body(new ParameterizedTypeReference<List<UsuarioDTO>>() {});

        // Devuelve una lista vacía si el backend no envía contenido.
        return usuarios != null ? usuarios : List.of();
    }

    // Realiza la baja lógica del usuario, conservándolo como inactivo.
    public void darDeBaja(String codigo, String motivoBaja) {
        validarCodigo(codigo);

        if (motivoBaja == null || motivoBaja.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un motivo de baja.");
        }

        BajaUsuarioDTO datosBaja = new BajaUsuarioDTO(motivoBaja);

        restClient.patch()
                .uri("/usuarios/{codigo}/baja", codigo.trim().toUpperCase())
                .headers(sesionUsuario::aplicarAutenticacion)
                .body(datosBaja)
                .retrieve()
                .toBodilessEntity();
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