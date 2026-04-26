package solveranet.backend.dto;

import java.util.UUID;

/**
 * DTO de respuesta para la autenticación y el registro.
 * Transporta el token JWT y la información esencial del usuario.
 */
public record AuthResponse(
        String token,
        UUID userId,
        String email,
        String role) {

}