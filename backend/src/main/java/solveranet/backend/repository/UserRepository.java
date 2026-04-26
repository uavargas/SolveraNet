package solveranet.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import solveranet.backend.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad User.
 * Soporta operaciones CRUD y búsqueda de usuarios por correo.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}