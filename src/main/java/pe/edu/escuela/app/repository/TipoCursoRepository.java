package pe.edu.escuela.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.escuela.app.model.TipoCurso;

public interface TipoCursoRepository extends JpaRepository<TipoCurso, Integer> {
}
