package pe.edu.escuela.app.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.escuela.app.model.PedidoDetalle;

public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, Integer> {

  /** Cursos mas vendidos: [nombre del curso, cupos vendidos, ingresos]. */
  @Query("SELECT d.nombreCurso, SUM(d.cantidad), SUM(d.subtotal) FROM PedidoDetalle d "
      + "GROUP BY d.nombreCurso ORDER BY SUM(d.cantidad) DESC, SUM(d.subtotal) DESC, d.nombreCurso ASC")
  List<Object[]> cursosMasVendidos(Pageable limite);

  /** Ingresos por tipo de curso: [tipo, unidades, ingresos]. */
  @Query("SELECT d.curso.tipoCurso.nombre, SUM(d.cantidad), SUM(d.subtotal) FROM PedidoDetalle d "
      + "GROUP BY d.curso.tipoCurso.nombre ORDER BY SUM(d.subtotal) DESC")
  List<Object[]> ingresosPorTipo();

  /** Inscripciones por modalidad (V = virtual, P = presencial): [modalidad, unidades, ingresos]. */
  @Query("SELECT d.curso.modalidad, SUM(d.cantidad), SUM(d.subtotal) FROM PedidoDetalle d "
      + "GROUP BY d.curso.modalidad ORDER BY SUM(d.cantidad) DESC")
  List<Object[]> ventasPorModalidad();

  /** Ingresos por docente: [docente, unidades, ingresos]. */
  @Query("SELECT CONCAT(d.curso.docente.nombres, ' ', d.curso.docente.apellidos), SUM(d.cantidad), SUM(d.subtotal) "
      + "FROM PedidoDetalle d GROUP BY d.curso.docente.nombres, d.curso.docente.apellidos "
      + "ORDER BY SUM(d.subtotal) DESC")
  List<Object[]> ingresosPorDocente(Pageable limite);
}
