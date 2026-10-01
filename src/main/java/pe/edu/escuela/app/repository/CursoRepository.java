package pe.edu.escuela.app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.escuela.app.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Integer> {

  List<Curso> findByDestacadoTrueAndEstadoOrderByIdCursoAsc(String estado);
}
