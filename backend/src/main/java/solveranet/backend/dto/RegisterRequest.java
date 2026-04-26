package solveranet.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para el registro de un nuevo usuario.
 * Incluye email, contraseña y nombre del rol que se desea asignar.
 */
public record RegisterRequest(
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    String password,

    @NotBlank(message = "Debe especificar un nombre de rol")
    String roleName
) {}