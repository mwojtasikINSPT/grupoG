package prog2.policia_backend.configs;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import prog2.policia_backend.security.AutorizadorPermisos;

// Configuración principal de Spring Security
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origin:http://localhost:8081}")
    private String allowedOrigin;

    // BCrypt se utiliza para guardar y verificar contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Conecta UserDetailsService con PasswordEncoder.
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    // Define cómo se protege la API
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AutorizadorPermisos autorizadorPermisos) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/estado").permitAll()
                .requestMatchers("/api/**").access(autorizadorPermisos)
                .anyRequest().denyAll())
                .httpBasic(httpBasic -> {
                });

        return http.build();
    }

    // Para permitir acceder desde otro puerto con el front
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Para que admita peticiones desde el front
        configuration.setAllowedOrigins(List.of(allowedOrigin));

        // métodos HTTP permitidos. OPTIONS para peticiones previas de control del
        // navegador
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Habilita los encabezados necesarios
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Permite que el navegador incluya credenciales
        configuration.setAllowCredentials(true);

        // Crea contenedor que asociará las reglas definidas con las rutas web la app
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        /////// Aplica esta config de seguridad a todos los endpoints ("/**")
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
