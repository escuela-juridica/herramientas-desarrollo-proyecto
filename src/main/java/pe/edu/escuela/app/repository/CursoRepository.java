package pe.edu.escuela.app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.escuela.app.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Integer> {

  @Query("SELECT c FROM Curso c JOIN FETCH c.tipoCurso WHERE c.destacado = true AND c.estado = :estado ORDER BY c.idCurso ASC")
  List<Curso> findByDestacadoTrueAndEstadoOrderByIdCursoAsc(@Param("estado") String estado);
}
