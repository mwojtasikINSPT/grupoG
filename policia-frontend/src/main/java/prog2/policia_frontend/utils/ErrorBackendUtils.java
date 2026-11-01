package prog2.policia_frontend.utils;

import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.dtos.RespuestaErrorDTO;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Recupera el mensaje funcional enviado por el backend para evitar que las
 * vistas oculten las reglas de negocio detrás de mensajes genéricos.
 */
public final class ErrorBackendUtils {

    private static final ObjectMapper MAPEADOR = new ObjectMapper();

    private ErrorBackendUtils() {
    }

    public static String obtenerMensaje(
            RestClientResponseException excepcion) {

        if (excepcion == null) {
            return "No se pudo completar la operación.";
        }

        try {
            RespuestaErrorDTO respuesta = excepcion.getResponseBodyAs(
                    RespuestaErrorDTO.class
            );

            if (respuesta != null
                    && respuesta.getMensaje() != null
                    && !respuesta.getMensaje().isBlank()) {
                return respuesta.getMensaje();
            }
        } catch (RuntimeException ignorada) {
            // La respuesta no utiliza el formato uniforme del backend.
        }

        String mensajeCrudo = obtenerMensajeDelJson(
                excepcion.getResponseBodyAsString()
        );

        if (mensajeCrudo != null) {
            return mensajeCrudo;
        }

        return switch (excepcion.getStatusCode().value()) {
            case 400 -> "Los datos enviados no son válidos.";
            case 401 -> "La sesión no es válida.";
            case 403 -> "No tiene permiso para realizar esta operación.";
            case 404 -> "No se encontró el recurso solicitado.";
            case 409 -> "La operación no puede realizarse en el estado actual.";
            default -> "No se pudo completar la operación.";
        };
    }

    private static String obtenerMensajeDelJson(String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank()) {
            return null;
        }

        try {
            JsonNode respuesta = MAPEADOR.readTree(cuerpo);
            JsonNode mensaje = respuesta.get("mensaje");

            if (mensaje != null && !mensaje.asText().isBlank()) {
                return mensaje.asText();
            }
        } catch (Exception ignorada) {
            // Se utiliza el mensaje general correspondiente al estado HTTP.
        }

        return null;
    }
}
