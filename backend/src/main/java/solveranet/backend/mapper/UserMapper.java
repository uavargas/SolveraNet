package solveranet.backend.mapper;

import org.springframework.stereotype.Component;
import solveranet.backend.dto.UserDto;
import solveranet.backend.model.User;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserDto(
            user.getId(),
            user.getEmail(),
            user.getRole() != null ? user.getRole().getName() : null,
            user.getIsActive(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
