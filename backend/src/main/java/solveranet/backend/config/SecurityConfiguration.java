package solveranet.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import solveranet.backend.security.JwtAuthenticationFilter;

/**
 * Configuración de seguridad de la aplicación.
 * Define reglas de acceso, políticas de sesión y agrega el filtro JWT.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Desactivamos CSRF (Cross-Site Request Forgery)
            // No lo necesitamos porque usaremos Tokens (Stateless), no cookies de sesión.
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. Configuración de Rutas (Endpoints)
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas: Todo lo que esté en /api/v1/auth/ (login, registro) no pide token
                .requestMatchers("/api/v1/auth/**").permitAll()
                // Rutas privadas: CUALQUIER otra petición debe estar autenticada
                .anyRequest().authenticated()
            )
            
            // 3. Gestión de Sesiones (Stateless)
            // Le decimos a Spring que no guarde el estado del usuario en memoria (sesión).
            // Cada petición es independiente y DEBE traer su propio token.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 4. Proveedor de Autenticación
            .authenticationProvider(authenticationProvider)
            
            // 5. Insertamos nuestro guardia (JwtAuthenticationFilter) ANTES del filtro estándar
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}