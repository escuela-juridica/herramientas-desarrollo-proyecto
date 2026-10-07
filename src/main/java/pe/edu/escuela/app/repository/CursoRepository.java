package pe.edu.escuela.app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.escuela.app.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Integer> {

  @Query("SELECT c FROM Curso c JOIN FETCH c.tipoCurso WHERE c.destacado = true AND c.estado = :estado ORDER BY c.idCurso ASC")
  List<Curso> findByDestacadoTrueAndEstadoOrderByIdCursoAsc(@Param("estado") String estado);

  @Query("SELECT c FROM Curso c JOIN FETCH c.tipoCurso WHERE c.estado = :estado ORDER BY c.idCurso ASC")
  List<Curso> findByEstadoOrderByIdCursoAsc(@Param("estado") String estado);

  @EntityGraph(attributePaths = {"tipoCurso", "docente"})
  List<Curso> findAllByOrderByIdCursoAsc();

  @EntityGraph(attributePaths = {"tipoCurso", "docente"})
  List<Curso> findByNombreContainingIgnoreCaseOrderByIdCursoAsc(String nombre);

  @Query("SELECT c FROM Curso c JOIN FETCH c.tipoCurso WHERE (:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND (:idTipoCurso IS NULL OR c.tipoCurso.idTipoCurso = :idTipoCurso) AND c.estado = :estado ORDER BY c.idCurso ASC")
  List<Curso> buscarConFiltros(@Param("nombre") String nombre, @Param("idTipoCurso") Integer idTipoCurso, @Param("estado") String estado);
}
