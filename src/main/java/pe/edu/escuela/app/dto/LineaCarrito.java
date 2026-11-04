package pe.edu.escuela.app.dto;

import java.math.BigDecimal;
import pe.edu.escuela.app.model.Curso;

/** Un curso del carrito con la cantidad de cupos que se quiere comprar. */
public class LineaCarrito {

  private final Curso curso;
  private final int cantidad;

  public LineaCarrito(Curso curso, int cantidad) {
    this.curso = curso;
    this.cantidad = cantidad;
  }

  public Curso getCurso() {
    return curso;
  }

  public int getCantidad() {
    return cantidad;
  }

  /** Precio de un cupo con el descuento de oferta vigente. */
  public BigDecimal getPrecioUnitario() {
    return curso.getPrecioFinal();
  }

  /** Lo que se paga por esta linea: precio con descuento por la cantidad. */
  public BigDecimal getSubtotal() {
    return curso.getPrecioFinal().multiply(BigDecimal.valueOf(cantidad));
  }

  /** Lo que costaria esta linea sin descuento. */
  public BigDecimal getSubtotalLista() {
    return curso.getPrecio().multiply(BigDecimal.valueOf(cantidad));
  }

  /** True si se pueden agregar mas cupos: el curso no tiene limite o aun quedan cupos. */
  public boolean isPuedeAumentar() {
    return curso.getCupos() == null || cantidad < curso.getCupos();
  }
}
