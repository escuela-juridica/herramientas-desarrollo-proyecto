package pe.edu.escuela.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.escuela.app.model.MensajeContacto;

public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Integer> {
}
