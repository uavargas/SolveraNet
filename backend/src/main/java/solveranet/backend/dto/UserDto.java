package solveranet.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(
    UUID id,
    String email,
    String role,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
