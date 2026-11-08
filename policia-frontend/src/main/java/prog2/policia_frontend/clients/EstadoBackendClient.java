package prog2.policia_frontend.clients;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
//usa BACKEND_URL: en NetBeans consulta el backend local y en Render la dir configurada 
public class EstadoBackendClient {

    private final String backendUrl;
    private final HttpClient cliente;

    public EstadoBackendClient(@Value("${backend.url}") String backendUrl) {
        this.backendUrl = backendUrl;
        this.cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public boolean estaDisponible() {
        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl + "/estado"))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        try {
            HttpResponse<String> respuesta = cliente.send(
                    solicitud, HttpResponse.BodyHandlers.ofString()
            );

            return respuesta.statusCode() == 200 && "OK".equals(respuesta.body());
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IOException excepcion) {
            return false;
        }
    }
}