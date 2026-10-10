
package prog2.policia_frontend.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import prog2.policia_frontend.dtos.UsuarioDTO;


 //interactua con el back  para comprobar el cód de usuario y la contraseña.

@Service//crea autm un obj de esto 
public class AutenticacionClient {

    private final RestClient restClient;

    //mensajero entre VAADIN y BACK
    public AutenticacionClient(
            @Value("${backend.url}") String backendUrl) {//busca el valor dentro propierties

        this.restClient = RestClient.builder()
                .baseUrl(backendUrl)
                .build();
    }

  //Recibe el cod y pass y devuelve usuariodto
    public UsuarioDTO autenticar(
            //basic auth
            String codigo,
            String password) {

        String codigoNormalizado
                = codigo.trim().toUpperCase();

        String endpoint
                = obtenerEndpoint(codigoNormalizado);

        return restClient.get()
                .uri(endpoint, codigoNormalizado)
                .headers(headers
                        -> headers.setBasicAuth(
                        codigoNormalizado,
                        password
                ))
                .retrieve()
                .body(UsuarioDTO.class);
    }

   //lo busca!!! si esta bien le da un dto si no noo
    private String obtenerEndpoint(String codigo) {

        if (codigo.startsWith("ADM")) {
            return "/administradores/{codigo}";
        }

        if (codigo.startsWith("INV")) {
            return "/investigadores/{codigo}";
        }

        if (codigo.startsWith("VIG")) {
            return "/vigilantes/{codigo}";
        }

        throw new IllegalArgumentException(
                "El código de usuario no tiene un formato válido."
        );
    }
}