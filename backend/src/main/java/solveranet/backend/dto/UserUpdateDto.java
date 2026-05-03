package solveranet.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateDto(
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "Formato de email inválido")
    String email,

    @NotBlank(message = "El rol es obligatorio")
    String role
) {}
