package solveranet.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import solveranet.backend.model.Role;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Role.
 * Permite buscar roles por nombre y realizar operaciones CRUD básicas.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    // Spring Data JPA crea la consulta SQL automáticamente solo con leer el nombre del método
    Optional<Role> findByName(String name);
}