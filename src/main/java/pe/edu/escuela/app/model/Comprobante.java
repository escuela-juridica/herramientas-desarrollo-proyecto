package pe.edu.escuela.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Boleta de venta de un pedido. El numero visible es la serie mas el id con ocho digitos (B001-00000001). */
@Entity
@Table(name = "comprobante")
public class Comprobante {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_comprobante")
  private Integer idComprobante;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_pedido", nullable = false, unique = true)
  private Pedido pedido;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "serie", nullable = false, length = 4)
  private String serie = "B001";

  @Column(name = "codigo_acceso", nullable = false, unique = true, length = 32)
  private String codigoAcceso;

  @Column(name = "fecha_emision", nullable = false)
  private LocalDateTime fechaEmision;

  @PrePersist
  protected void alCrear() {
    if (this.fechaEmision == null) {
      this.fechaEmision = LocalDateTime.now();
    }
  }

  public String getNumero() {
    return serie.trim() + "-" + String.format("%08d", idComprobante);
  }

  public Integer getIdComprobante() {
    return idComprobante;
  }

  public Pedido getPedido() {
    return pedido;
  }

  public void setPedido(Pedido pedido) {
    this.pedido = pedido;
  }

  public String getSerie() {
    return serie;
  }

  public void setSerie(String serie) {
    this.serie = serie;
  }

  public String getCodigoAcceso() {
    return codigoAcceso;
  }

  public void setCodigoAcceso(String codigoAcceso) {
    this.codigoAcceso = codigoAcceso;
  }

  public LocalDateTime getFechaEmision() {
    return fechaEmision;
  }

  public void setFechaEmision(LocalDateTime fechaEmision) {
    this.fechaEmision = fechaEmision;
  }
}
