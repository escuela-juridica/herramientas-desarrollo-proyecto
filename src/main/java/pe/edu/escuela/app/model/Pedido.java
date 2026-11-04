package pe.edu.escuela.app.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Compra de uno o mas cursos. Los montos incluyen IGV (total = subtotal - descuentoTotal). */
@Entity
@Table(name = "pedido")
public class Pedido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_pedido")
  private Integer idPedido;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_cliente", nullable = false)
  private Cliente cliente;

  @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
  private BigDecimal subtotal = BigDecimal.ZERO;

  @Column(name = "descuento_total", nullable = false, precision = 10, scale = 2)
  private BigDecimal descuentoTotal = BigDecimal.ZERO;

  @Column(name = "total", nullable = false, precision = 10, scale = 2)
  private BigDecimal total = BigDecimal.ZERO;

  @Column(name = "igv", nullable = false, precision = 10, scale = 2)
  private BigDecimal igv = BigDecimal.ZERO;

  @Column(name = "medio_pago", nullable = false, length = 20)
  private String medioPago;

  @Column(name = "fecha_pedido", nullable = false)
  private LocalDateTime fechaPedido;

  @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
  @OrderBy("idDetalle ASC")
  private List<PedidoDetalle> detalles = new ArrayList<>();

  @PrePersist
  protected void alCrear() {
    if (this.fechaPedido == null) {
      this.fechaPedido = LocalDateTime.now();
    }
  }

  public void agregarDetalle(PedidoDetalle detalle) {
    detalle.setPedido(this);
    detalles.add(detalle);
  }

  /** Monto sin IGV (base imponible). */
  public BigDecimal getBaseImponible() {
    return total.subtract(igv);
  }

  public Integer getIdPedido() {
    return idPedido;
  }

  public void setIdPedido(Integer idPedido) {
    this.idPedido = idPedido;
  }

  public Cliente getCliente() {
    return cliente;
  }

  public void setCliente(Cliente cliente) {
    this.cliente = cliente;
  }

  public BigDecimal getSubtotal() {
    return subtotal;
  }

  public void setSubtotal(BigDecimal subtotal) {
    this.subtotal = subtotal;
  }

  public BigDecimal getDescuentoTotal() {
    return descuentoTotal;
  }

  public void setDescuentoTotal(BigDecimal descuentoTotal) {
    this.descuentoTotal = descuentoTotal;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public void setTotal(BigDecimal total) {
    this.total = total;
  }

  public BigDecimal getIgv() {
    return igv;
  }

  public void setIgv(BigDecimal igv) {
    this.igv = igv;
  }

  public String getMedioPago() {
    return medioPago;
  }

  public void setMedioPago(String medioPago) {
    this.medioPago = medioPago;
  }

  public LocalDateTime getFechaPedido() {
    return fechaPedido;
  }

  public void setFechaPedido(LocalDateTime fechaPedido) {
    this.fechaPedido = fechaPedido;
  }

  public List<PedidoDetalle> getDetalles() {
    return detalles;
  }
}
