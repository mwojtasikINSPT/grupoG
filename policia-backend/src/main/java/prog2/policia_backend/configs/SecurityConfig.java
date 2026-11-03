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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // apaga la protección CSRF (en APIs REST sin estado)
                .csrf(csrf -> csrf.disable())
                // habilita el flujo cruzado (CORS) para Vaadin
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Autenticación basada en permisos
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/roles", "/api/roles/**")
                .hasRole("ADMINISTRADOR")
                // Creación, modificación o eliminación requieren escritura - cambio password
                .requestMatchers(HttpMethod.PATCH, "/api/usuarios/password")
                .authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/usuarios/*/password")
                .hasRole("ADMINISTRADOR")
                // Permite consultar contratos de un vigilante
                .requestMatchers(HttpMethod.GET, "/api/contratos-vigilancia", "/api/contratos-vigilancia/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "LECTURA_CONTRATO_VIGILANCIA", "ESCRITURA_CONTRATO_VIGILANCIA", "LECTURA_DATOS_PROPIOS_CONTRATO_VIGILANCIA")
                // Consultas generales de lectura sobre la API
                .requestMatchers(HttpMethod.GET, "/api/usuarios", "/api/usuarios/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "LECTURA_USUARIO", "LECTURA_DATOS_PROPIOS_USUARIO")
                .requestMatchers(HttpMethod.GET, "/api/asaltantes", "/api/asaltantes/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "LECTURA_ASALTANTE", "ESCRITURA_ASALTANTE", "LECTURA_DATOS_PROPIOS_ASALTANTE")
                .requestMatchers(HttpMethod.GET, "/api/asaltos", "/api/asaltos/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "LECTURA_ASALTO", "ESCRITURA_ASALTO", "LECTURA_DATOS_PROPIOS_ASALTO")
                .requestMatchers(HttpMethod.GET, "/api/bandas", "/api/bandas/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "LECTURA_BANDA", "ESCRITURA_BANDA", "LECTURA_DATOS_PROPIOS_BANDA")
                .requestMatchers(HttpMethod.GET, "/api/casos-judiciales", "/api/casos-judiciales/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "LECTURA_CASO_JUDICIAL", "ESCRITURA_CASO_JUDICIAL", "LECTURA_DATOS_PROPIOS_CASO_JUDICIAL")
                .requestMatchers(HttpMethod.GET, "/api/entidades-bancarias", "/api/entidades-bancarias/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "LECTURA_ENTIDAD_BANCARIA", "ESCRITURA_ENTIDAD_BANCARIA", "LECTURA_DATOS_PROPIOS_ENTIDAD_BANCARIA")
                .requestMatchers(HttpMethod.GET, "/api/jueces", "/api/jueces/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "LECTURA_JUEZ", "ESCRITURA_JUEZ", "LECTURA_DATOS_PROPIOS_JUEZ")
                .requestMatchers(HttpMethod.GET, "/api/sucursales", "/api/sucursales/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "LECTURA_SUCURSAL", "ESCRITURA_SUCURSAL", "LECTURA_DATOS_PROPIOS_SUCURSAL")
                .requestMatchers(HttpMethod.GET, "/api/sentencias-asaltantes", "/api/sentencias-asaltantes/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "LECTURA_SENTENCIA_ASALTANTE", "ESCRITURA_SENTENCIA_ASALTANTE", "LECTURA_DATOS_PROPIOS_SENTENCIA_ASALTANTE")
                .requestMatchers("/api/usuarios", "/api/usuarios/**")
                .hasRole("ADMINISTRADOR")
                .requestMatchers("/api/contratos-vigilancia", "/api/contratos-vigilancia/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "ESCRITURA_CONTRATO_VIGILANCIA")
                .requestMatchers("/api/asaltantes", "/api/asaltantes/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "ESCRITURA_ASALTANTE")
                .requestMatchers("/api/asaltos", "/api/asaltos/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "ESCRITURA_ASALTO")
                .requestMatchers("/api/bandas", "/api/bandas/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "ESCRITURA_BANDA")
                .requestMatchers("/api/casos-judiciales", "/api/casos-judiciales/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "ESCRITURA_CASO_JUDICIAL")
                .requestMatchers("/api/entidades-bancarias", "/api/entidades-bancarias/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "ESCRITURA_ENTIDAD_BANCARIA")
                .requestMatchers("/api/jueces", "/api/jueces/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "ESCRITURA_JUEZ")
                .requestMatchers("/api/sucursales", "/api/sucursales/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "ESCRITURA_SUCURSAL")
                .requestMatchers("/api/sentencias-asaltantes", "/api/sentencias-asaltantes/**")
                .hasAnyAuthority("ROLE_ADMINISTRADOR", "ESCRITURA_SENTENCIA_ASALTANTE")
                .requestMatchers("/api/**").denyAll()
                .anyRequest().authenticated())
                // Login mediante usuario/código + contraseña
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
