package solveranet.backend.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import solveranet.backend.dto.AuthRequest;
import solveranet.backend.dto.AuthResponse;
import solveranet.backend.dto.RegisterRequest;
import solveranet.backend.model.Role;
import solveranet.backend.model.User;
import solveranet.backend.repository.RoleRepository;
import solveranet.backend.repository.UserRepository;
import solveranet.backend.security.JwtService;

/**
 * Servicio de autenticación que gestiona registro e inicio de sesión.
 * Combina acceso a repositorios, encriptación de contraseñas y generación de JWT.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ── CONSTRUCTOR MANUAL (Mejor práctica para inyección de dependencias) ──
    public AuthService(UserRepository userRepository, RoleRepository roleRepository, 
                       PasswordEncoder passwordEncoder, JwtService jwtService, 
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * RF-01: Registro de un nuevo usuario.
     * Usamos @Transactional para que, si algo falla, no se guarde basura en la BD.
     */
    /**
     * Registra un nuevo usuario y devuelve el token JWT generado.
     * Valida que el email no exista y que el rol solicitado sea válido.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Verificamos si el correo ya existe
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado en el sistema");
        }

        // 2. Buscamos el rol en la base de datos
        Role userRole = roleRepository.findByName(request.roleName())
                .orElseThrow(() -> new IllegalArgumentException("Rol no válido: " + request.roleName()));

        // 3. Construimos el nuevo usuario encriptando su contraseña
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // SEC-05: ¡Nunca en texto plano!
                .role(userRole)
                .isActive(true)
                .build();

        // 4. Guardamos en la base de datos
        userRepository.save(user);

        // 5. Generamos el token de bienvenida
        String jwtToken = jwtService.generateToken(user);

        // 6. Devolvemos la respuesta al Frontend
        return new AuthResponse(jwtToken, user.getId(), user.getEmail(), userRole.getName());
    }

    /**
     * Autentica las credenciales del usuario y devuelve un token JWT válido.
     */
    public AuthResponse authenticate(AuthRequest request) {
        // 1. El AuthenticationManager hace el trabajo duro: busca al usuario y compara el BCrypt
        // Si la contraseña es incorrecta, esto lanza una excepción automáticamente (403 Forbidden).
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // 2. Si llegamos aquí, las credenciales son correctas. Buscamos al usuario.
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(); // Ya sabemos que existe porque el paso anterior pasó

        // 3. Generamos su nuevo token
        String jwtToken = jwtService.generateToken(user);

        // 4. Respondemos con sus datos
        return new AuthResponse(jwtToken, user.getId(), user.getEmail(), user.getRole().getName());
    }
}