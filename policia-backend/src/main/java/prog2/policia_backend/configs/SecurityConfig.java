package prog2.policia_backend.configs;

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

// Configuración principal de Spring Security
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // BCrypt se utiliza para guardar y verificar contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Conecta UserDetailsService con PasswordEncoder.
    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider
                = new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    // Define cómo se protege la API.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // API REST: no necesitamos CSRF con este esquema - Cross-Site Request Forgery, o falsificación de solicitudes entre sitios
                .csrf(csrf -> csrf.disable())
                // No usamos sesiones HTTP
                .sessionManagement(session
                        -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))
                // autenticación
                .authorizeHttpRequests(auth -> auth
                // El endpoint de un vigilante podrá ser accedido
                // por los tres roles; luego @PreAuthorize decidirá
                // si el vigilante puede ver ese ID.
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/vigilantes/*")
                .hasAnyRole(
                        "VIGILANTE",
                        "INVESTIGADOR",
                        "ADMINISTRADOR")
                // Los investigadores y administradores pueden consultar todo.
                .requestMatchers(HttpMethod.GET, "/api/**")
                .hasAnyRole(
                        "INVESTIGADOR",
                        "ADMINISTRADOR")
                // Solo administradores pueden crear, modificar o eliminar.
                .requestMatchers("/api/**")
                .hasRole("ADMINISTRADOR")
                .requestMatchers("/error")
                .permitAll()
                .anyRequest()
                .authenticated()
                )
                // Login mediante usuario/código + contraseña
                .httpBasic(httpBasic -> {
                });

        return http.build();
    }
}
