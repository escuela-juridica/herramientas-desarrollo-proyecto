package pe.edu.escuela.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.escuela.app.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

  /** Ventas por mes: [anio, mes, total vendido, descuento otorgado, cantidad de pedidos]. */
  @Query("SELECT YEAR(p.fechaPedido), MONTH(p.fechaPedido), SUM(p.total), SUM(p.descuentoTotal), COUNT(p) "
      + "FROM Pedido p WHERE p.fechaPedido >= :desde "
      + "GROUP BY YEAR(p.fechaPedido), MONTH(p.fechaPedido) "
      + "ORDER BY YEAR(p.fechaPedido), MONTH(p.fechaPedido)")
  List<Object[]> resumenPorMes(@Param("desde") LocalDateTime desde);

  /** Totales generales: [total vendido, descuento otorgado, cantidad de pedidos]. */
  @Query("SELECT COALESCE(SUM(p.total), 0), COALESCE(SUM(p.descuentoTotal), 0), COUNT(p) FROM Pedido p")
  List<Object[]> totalesGenerales();
}
