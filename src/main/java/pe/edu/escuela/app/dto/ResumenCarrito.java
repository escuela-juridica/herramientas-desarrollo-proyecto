package pe.edu.escuela.app.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Lineas del carrito con sus montos. Los precios incluyen IGV (18 %):
 * total = subtotal - descuento, y el IGV es la parte del total que corresponde al impuesto.
 */
public class ResumenCarrito {

  private static final BigDecimal FACTOR_IGV = new BigDecimal("1.18");

  private final List<LineaCarrito> lineas;
  private final BigDecimal subtotal;
  private final BigDecimal descuento;
  private final BigDecimal total;
  private final BigDecimal igv;

  private ResumenCarrito(List<LineaCarrito> lineas, BigDecimal subtotal, BigDecimal total, BigDecimal igv) {
    this.lineas = lineas;
    this.subtotal = subtotal;
    this.descuento = subtotal.subtract(total);
    this.total = total;
    this.igv = igv;
  }

  public static ResumenCarrito de(List<LineaCarrito> lineas) {
    BigDecimal subtotal = BigDecimal.ZERO;
    BigDecimal total = BigDecimal.ZERO;
    for (LineaCarrito linea : lineas) {
      subtotal = subtotal.add(linea.getSubtotalLista());
      total = total.add(linea.getSubtotal());
    }
    BigDecimal igv = total.subtract(total.divide(FACTOR_IGV, 2, RoundingMode.HALF_UP));
    return new ResumenCarrito(lineas, subtotal, total, igv);
  }

  public List<LineaCarrito> getLineas() {
    return lineas;
  }

  public BigDecimal getSubtotal() {
    return subtotal;
  }

  public BigDecimal getDescuento() {
    return descuento;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public BigDecimal getIgv() {
    return igv;
  }

  public BigDecimal getBaseImponible() {
    return total.subtract(igv);
  }

  public boolean isVacio() {
    return lineas.isEmpty();
  }

  /** Total de cupos en el carrito (suma de las cantidades). */
  public int getCantidad() {
    return lineas.stream().mapToInt(LineaCarrito::getCantidad).sum();
  }
}
