package prog2.policia_frontend.clients;

import java.util.List;
import java.util.function.Consumer;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.CasoDefensaDTO;
import prog2.policia_frontend.dtos.DefendidoDTO;
import prog2.policia_frontend.security.SesionUsuario;

@Service
@VaadinSessionScope
public class DefensaClient {
    private final RestClient restClient;
    private final Consumer<HttpHeaders> autenticacion;

    public DefensaClient(@Value("${backend.url}") String backendUrl, SesionUsuario sesion) {
        restClient = RestClient.builder().baseUrl(backendUrl).build();
        autenticacion = new Consumer<HttpHeaders>() {
            @Override
            public void accept(HttpHeaders headers) { sesion.aplicarAutenticacion(headers); }
        };
    }

    public List<DefendidoDTO> listarDefendidos() {
        List<DefendidoDTO> resultado = restClient.get().uri("/defensa/defendidos")
                .headers(autenticacion).retrieve().body(new ParameterizedTypeReference<List<DefendidoDTO>>() {});
        return resultado == null ? List.of() : resultado;
    }

    public DefendidoDTO consultarDefendido(String codigo) {
        return restClient.get().uri("/defensa/defendidos/{codigo}", codigo)
                .headers(autenticacion).retrieve().body(DefendidoDTO.class);
    }

    public List<CasoDefensaDTO> listarCasos(String codigo) {
        List<CasoDefensaDTO> resultado = restClient.get()
                .uri("/defensa/defendidos/{codigo}/casos-judiciales", codigo).headers(autenticacion)
                .retrieve().body(new ParameterizedTypeReference<List<CasoDefensaDTO>>() {});
        return resultado == null ? List.of() : resultado;
    }

    public CasoDefensaDTO consultarCaso(String codigo, String caso) {
        return restClient.get().uri("/defensa/defendidos/{codigo}/casos-judiciales/{caso}", codigo, caso)
                .headers(autenticacion).retrieve().body(CasoDefensaDTO.class);
    }
}
