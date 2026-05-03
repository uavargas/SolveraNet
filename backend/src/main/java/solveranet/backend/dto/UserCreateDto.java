package solveranet.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateDto(
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "Formato de email inválido")
    String email,

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    String password,

    @NotBlank(message = "El rol es obligatorio")
    String role
) {}
