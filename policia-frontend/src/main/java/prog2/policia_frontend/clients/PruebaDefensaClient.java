package prog2.policia_frontend.clients;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.PresentacionPruebaDefensaDTO;
import prog2.policia_frontend.dtos.PruebaDefensaDTO;
import prog2.policia_frontend.security.SesionUsuario;

@Service
@VaadinSessionScope
public class PruebaDefensaClient {
    public static final int MAXIMO_ARCHIVO = 2097152;
    private final RestClient restClient;
    private final Consumer<HttpHeaders> autenticacion;

    public PruebaDefensaClient(@Value("${backend.url}") String backendUrl, SesionUsuario sesion) {
        restClient = RestClient.builder().baseUrl(backendUrl).build();
        autenticacion = new Consumer<HttpHeaders>() {
            @Override
            public void accept(HttpHeaders headers) { sesion.aplicarAutenticacion(headers); }
        };
    }

    public List<PruebaDefensaDTO> consultar(String asaltante, String caso) {
        List<PruebaDefensaDTO> resultado = restClient.get()
                .uri("/pruebas-defensa?asaltanteCodigo={asaltante}&casoCodigo={caso}", asaltante, caso)
                .headers(autenticacion).retrieve().body(new ParameterizedTypeReference<List<PruebaDefensaDTO>>() {});
        return resultado == null ? List.of() : resultado;
    }

    public PruebaDefensaDTO presentar(PresentacionPruebaDefensaDTO datos, String nombre, String tipo, byte[] contenido) {
        if (datos.getDescripcion() == null || datos.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        if (datos.getDescripcion().length() > 4000) {
            throw new IllegalArgumentException("La descripción no puede superar los 4000 caracteres.");
        }
        if (contenido == null) {
            return restClient.post().uri("/pruebas-defensa").headers(autenticacion)
                    .contentType(MediaType.APPLICATION_JSON).body(datos).retrieve().body(PruebaDefensaDTO.class);
        }
        validarAdjuntoDeclarado(nombre, contenido.length, tipo);
        MultiValueMap<String, Object> partes = new LinkedMultiValueMap<>();
        partes.add("asaltanteCodigo", datos.getAsaltanteCodigo());
        partes.add("casoCodigo", datos.getCasoCodigo());
        partes.add("descripcion", datos.getDescripcion());
        partes.add("archivo", new ByteArrayResource(contenido) {
            @Override
            public String getFilename() { return nombre; }
        });
        return restClient.post().uri("/pruebas-defensa").headers(autenticacion)
                .contentType(MediaType.MULTIPART_FORM_DATA).body(partes).retrieve().body(PruebaDefensaDTO.class);
    }

    public ResponseEntity<byte[]> descargar(Long id) {
        return restClient.get().uri("/pruebas-defensa/{id}/archivo", id).headers(autenticacion)
                .retrieve().toEntity(byte[].class);
    }

    public void validarAdjuntoDeclarado(String nombre, long tamano, String tipo) {
        if (tamano > MAXIMO_ARCHIVO) { throw new IllegalArgumentException("El archivo no puede superar los 2 MB."); }
        if (tamano == 0) { throw new IllegalArgumentException("El archivo está vacío."); }
        String extension = nombre == null ? "" : nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        String esperado;
        switch (extension) {
            case "pdf": esperado = "application/pdf"; break;
            case "jpg":
            case "jpeg": esperado = "image/jpeg"; break;
            case "png": esperado = "image/png"; break;
            case "txt": esperado = "text/plain"; break;
            default: throw new IllegalArgumentException("Los formatos permitidos son PDF, JPG, JPEG, PNG y TXT.");
        }
        if (tipo != null && !tipo.isBlank()) {
            String declarado = tipo.split(";")[0].trim().toLowerCase(Locale.ROOT);
            if (!declarado.equals(esperado) && !declarado.equals("application/octet-stream")) {
                throw new IllegalArgumentException("El tipo de archivo seleccionado no coincide con su extensión.");
            }
        }
    }
}
