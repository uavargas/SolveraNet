package solveranet.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de solicitud para el inicio de sesión.
 * Contiene las credenciales necesarias para autenticar a un usuario.
 */
public record AuthRequest(

        @NotBlank(message = "El email no puede estar vacío") 
        @Email(message = "Formato de email inválido") String email,
        
        @NotBlank(message = "La contraseña no puede estar vacía") 
        String password

) {

}
