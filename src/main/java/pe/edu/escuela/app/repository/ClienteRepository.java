package pe.edu.escuela.app.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.escuela.app.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

  Optional<Cliente> findByDocumento(String documento);
}
