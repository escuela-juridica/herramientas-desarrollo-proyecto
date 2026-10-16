package pe.edu.escuela.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.escuela.app.model.Docente;

public interface DocenteRepository extends JpaRepository<Docente, Integer> {
}
