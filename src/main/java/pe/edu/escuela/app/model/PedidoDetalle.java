package pe.edu.escuela.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Un curso dentro de un pedido: cantidad de cupos y nombre y precios (por cupo) que tenia al momento de la compra. */
@Entity
@Table(name = "pedido_detalle")
public class PedidoDetalle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_detalle")
  private Integer idDetalle;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_pedido", nullable = false)
  private Pedido pedido;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_curso", nullable = false)
  private Curso curso;

  @Column(name = "nombre_curso", nullable = false, length = 150)
  private String nombreCurso;

  @Column(name = "cantidad", nullable = false)
  private Integer cantidad = 1;

  @Column(name = "precio_lista", nullable = false, precision = 10, scale = 2)
  private BigDecimal precioLista;

  @Column(name = "descuento_porcentaje", nullable = false, precision = 5, scale = 2)
  private BigDecimal descuentoPorcentaje = BigDecimal.ZERO;

  @Column(name = "precio_final", nullable = false, precision = 10, scale = 2)
  private BigDecimal precioFinal;

  @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
  private BigDecimal subtotal;

  public Integer getIdDetalle() {
    return idDetalle;
  }

  public Pedido getPedido() {
    return pedido;
  }

  public void setPedido(Pedido pedido) {
    this.pedido = pedido;
  }

  public Curso getCurso() {
    return curso;
  }

  public void setCurso(Curso curso) {
    this.curso = curso;
  }

  public String getNombreCurso() {
    return nombreCurso;
  }

  public void setNombreCurso(String nombreCurso) {
    this.nombreCurso = nombreCurso;
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public void setCantidad(Integer cantidad) {
    this.cantidad = cantidad;
  }

  public BigDecimal getSubtotal() {
    return subtotal;
  }

  public void setSubtotal(BigDecimal subtotal) {
    this.subtotal = subtotal;
  }

  public BigDecimal getPrecioLista() {
    return precioLista;
  }

  public void setPrecioLista(BigDecimal precioLista) {
    this.precioLista = precioLista;
  }

  public BigDecimal getDescuentoPorcentaje() {
    return descuentoPorcentaje;
  }

  public void setDescuentoPorcentaje(BigDecimal descuentoPorcentaje) {
    this.descuentoPorcentaje = descuentoPorcentaje;
  }

  public BigDecimal getPrecioFinal() {
    return precioFinal;
  }

  public void setPrecioFinal(BigDecimal precioFinal) {
    this.precioFinal = precioFinal;
  }
}
