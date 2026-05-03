package solveranet.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import solveranet.backend.dto.AuthRequest;
import solveranet.backend.dto.AuthResponse;
import solveranet.backend.dto.RegisterRequest;
import solveranet.backend.service.AuthService;

/**
 * Controlador REST para la autenticación de usuarios.
 * Expone endpoints de registro e inicio de sesión bajo /api/v1/auth.
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    // ── CONSTRUCTOR MANUAL (Mejor práctica para inyección de dependencias) ──
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint para registrar un nuevo usuario.
     * URL: POST http://localhost:8080/api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        // Delegamos toda la lógica  al servicio
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Endpoint para iniciar sesión.
     * URL: POST http://localhost:8080/api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(
            @Valid @RequestBody AuthRequest request
    ) {
        // Si el login falla, el servicio lanzará una excepción y Spring devolverá un 403.
        return ResponseEntity.ok(authService.authenticate(request));
    }
}
