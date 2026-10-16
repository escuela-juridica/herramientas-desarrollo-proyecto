package pe.edu.escuela.app.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.escuela.app.model.Administrador;

public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {

  Optional<Administrador> findByCorreo(String correo);
  Optional<Administrador> findFirstByOrderByIdAdminAsc();
}
