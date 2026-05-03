package solveranet.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import solveranet.backend.security.JwtAuthenticationFilter;

import java.util.List;

/**
 * =====================================================================
 * CAPA: Infrastructure > Security
 * RESPONSABILIDAD: Definir y publicar las reglas de seguridad HTTP de la
 *                  aplicación como beans de Spring gestionados por el
 *                  contenedor de IoC.
 *
 * DECISIONES DE ARQUITECTURA (ADR):
 *  - Autenticación sin estado (Stateless): Se delega la identidad del
 *    usuario al portador del JWT, eliminando la dependencia de sesiones
 *    en servidor. Esto habilita escalabilidad horizontal sin sticky sessions.
 *
 *  - Autorización basada en roles (RBAC): Los permisos de ruta se
 *    declaran de forma centralizada aquí. Para lógica de autorización
 *    más granular a nivel de método, se usa @PreAuthorize junto con
 *    @EnableMethodSecurity.
 *
 *  - CORS explícito: Se rechaza la configuración por defecto de Spring
 *    para tener control total sobre los orígenes permitidos por entorno.
 *
 * DEPENDENCIAS INYECTADAS:
 *  - JwtAuthenticationFilter: Interceptor que valida el Bearer token
 *    en cada petición entrante antes del filtro estándar de Spring.
 *  - AuthenticationProvider: Estrategia de autenticación (UserDetails + BCrypt).
 *
 * @author  Alonso Vargas
 
 * =====================================================================
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity           // Habilita @PreAuthorize / @PostAuthorize a nivel de método
@RequiredArgsConstructor
public class SecurityConfiguration {

    // ----------------------------------------------------------------
    // CONSTANTES DE RUTAS
    // Centralizar los patrones evita "magic strings" dispersos en la config.
    // Si una ruta cambia, se actualiza en un único lugar.
    // ----------------------------------------------------------------

    private static final String AUTH_WHITELIST_LOGIN    = "/api/v1/auth/login";
    private static final String AUTH_WHITELIST_REGISTER = "/api/v1/auth/register";
    private static final String USERS_ADMIN_PATH        = "/api/v1/users/**";

    // ----------------------------------------------------------------
    // INYECCIÓN DE DEPENDENCIAS (via constructor — Lombok @RequiredArgsConstructor)
    // Se prefiere inyección por constructor sobre @Autowired para:
    //   1. Inmutabilidad (campos final).
    //   2. Testabilidad (mock fácil sin contexto de Spring).
    //   3. Detección temprana de dependencias circulares en arranque.
    // ----------------------------------------------------------------

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider  authenticationProvider;

    // ----------------------------------------------------------------
    // CONFIGURACIÓN DE CORS
    // ----------------------------------------------------------------
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigin;

    // ================================================================
    // BEAN PRINCIPAL: SecurityFilterChain
    // ================================================================

    /**
     * Configura y publica la cadena de filtros de seguridad HTTP.
     *
     * <p><b>Orden de evaluación de los filtros de Spring Security:</b>
     * <pre>
     *   [Request] → CorsFilter → JwtAuthenticationFilter → UsernamePasswordAuthFilter → ...
     * </pre>
     *
     * <p><b>Matriz de acceso declarada:</b>
     * <pre>
     *   POST /api/v1/auth/login      → PUBLIC   (obtención de token)
     *   POST /api/v1/auth/register   → PUBLIC   (alta de usuario)
     *   ANY  /api/v1/users/**        → ROLE_ADMIN (gestión de usuarios)
     *   ANY  /**                     → AUTHENTICATED (cualquier usuario con token válido)
     * </pre>
     *
     * @param http Builder de configuración HTTP de Spring Security.
     * @return La cadena de filtros configurada.
     * @throws Exception Si falla la construcción de la cadena de filtros.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // --- CORS ---
            // Se delega en el bean corsConfigurationSource() definido más abajo.
            // Debe declararse ANTES de csrf para que el preflight OPTIONS
            // sea respondido correctamente sin pasar por validación CSRF.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // --- CSRF ---
            // Deshabilitado intencionalmente: los ataques CSRF explotan cookies
            // de sesión. Al usar JWT en cabecera Authorization (Bearer),
            // no existe superficie de ataque CSRF que proteger.
            .csrf(AbstractHttpConfigurer::disable)

            // --- AUTORIZACIÓN DE RUTAS ---
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**")
                    .permitAll()
                .requestMatchers(USERS_ADMIN_PATH)
                    .hasRole("ADMIN")
                .anyRequest()
                    .authenticated()
            )

            // --- GESTIÓN DE SESIÓN (Stateless) ---
            // Spring NO creará ni usará HttpSession.
            // El estado de autenticación vive únicamente en el JWT del cliente.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // --- PROVEEDOR DE AUTENTICACIÓN ---
            // Delega la validación de credenciales (usuario + contraseña) al
            // bean AuthenticationProvider definido en ApplicationConfig.
            .authenticationProvider(authenticationProvider)

            // --- FILTRO JWT ---
            // Se inserta ANTES del filtro estándar de usuario/contraseña para que
            // Spring Security reconozca al usuario por su token antes de
            // intentar cualquier otra estrategia de autenticación.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ================================================================
    // BEAN AUXILIAR: CorsConfigurationSource
    // ================================================================

    /**
     * Define la política Cross-Origin Resource Sharing (CORS) de la API.
     *
     * <p><b>¿Por qué un bean separado y no cors().disable()?</b>
     * Deshabilitar CORS completamente permitiría peticiones desde cualquier
     * origen, lo cual es un riesgo de seguridad en producción. Esta
     * configuración explícita aplica el principio de mínimo privilegio.
     *
     * <p><b>Nota sobre {@code allowCredentials(true)}:</b>
     * Cuando se envían credenciales (cookies, cabeceras de autorización),
     * {@code allowedOrigins} NO puede ser {@code "*"}. Debe especificarse
     * el origen exacto, como se hace aquí con {@code allowedOrigin}.
     *
     * @return Fuente de configuración CORS registrada para todos los paths.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Origen(es) permitido(s) — leído desde properties según el entorno activo.
        config.setAllowedOrigins(List.of(allowedOrigin));

        // Métodos HTTP permitidos. OPTIONS es obligatorio para el Preflight request
        // que los navegadores envían automáticamente antes de peticiones cross-origin.
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Cabeceras que el cliente puede incluir en su petición.
        // "Authorization" es necesaria para enviar el Bearer token.
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // Cabeceras de respuesta que el browser expone al código JavaScript del cliente.
        // Útil si el backend renueva el token en cada respuesta.
        config.setExposedHeaders(List.of("Authorization"));

        // Permite que el navegador incluya credenciales (ej. Bearer token en Authorization).
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}