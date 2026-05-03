package solveranet.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import solveranet.backend.dto.UserCreateDto;
import solveranet.backend.dto.UserDto;
import solveranet.backend.dto.UserUpdateDto;
import solveranet.backend.exception.ResourceNotFoundException;
import solveranet.backend.mapper.UserMapper;
import solveranet.backend.model.Role;
import solveranet.backend.model.User;
import solveranet.backend.repository.RoleRepository;
import solveranet.backend.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto createUser(UserCreateDto dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new IllegalArgumentException("El email ya está en uso");
        }

        Role role = roleRepository.findByName(dto.role().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + dto.role()));

        User user = User.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(role)
                .isActive(true)
                .build();

        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto updateUser(UUID id, UserUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (!user.getEmail().equals(dto.email()) && userRepository.findByEmail(dto.email()).isPresent()) {
            throw new IllegalArgumentException("El email ya está en uso por otro usuario");
        }

        Role role = roleRepository.findByName(dto.role().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + dto.role()));

        user.setEmail(dto.email());
        user.setRole(role);

        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Transactional
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        
        user.setIsActive(!user.getIsActive()); // Toggle the active state
        userRepository.save(user);
    }
}
