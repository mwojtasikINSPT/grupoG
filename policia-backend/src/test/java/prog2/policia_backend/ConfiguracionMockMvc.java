package prog2.policia_backend;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@TestConfiguration
public class ConfiguracionMockMvc {
    @Bean
    MockMvcBuilderCustomizer rutaServletEnPeticiones() {
        return builder -> builder.defaultRequest(get("/").with(request -> {
            request.setServletPath(request.getRequestURI().substring(request.getContextPath().length()));
            return request;
        }));
    }
}
