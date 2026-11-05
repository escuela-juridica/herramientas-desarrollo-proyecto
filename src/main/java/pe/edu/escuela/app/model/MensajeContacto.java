package pe.edu.escuela.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Mensaje enviado desde el formulario de Contacto (pedido, opinion o consulta). Estado: N = nuevo, L = leido. */
@Entity
@Table(name = "mensaje_contacto")
public class MensajeContacto {

  public static final String TIPO_PEDIDO = "PEDIDO";
  public static final String TIPO_OPINION = "OPINION";
  public static final String TIPO_CONSULTA = "CONSULTA";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_mensaje")
  private Integer idMensaje;

  @Column(name = "nombres", nullable = false, length = 100)
  private String nombres;

  @Column(name = "correo", nullable = false, length = 150)
  private String correo;

  @Column(name = "telefono", length = 20)
  private String telefono;

  @Column(name = "tipo", nullable = false, length = 10)
  private String tipo;

  @Column(name = "mensaje", nullable = false, length = 1000)
  private String mensaje;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "estado", nullable = false, length = 1)
  private String estado = "N";

  @Column(name = "fecha_envio", nullable = false, updatable = false)
  private LocalDateTime fechaEnvio;

  @PrePersist
  protected void alCrear() {
    this.fechaEnvio = LocalDateTime.now();
  }

  public Integer getIdMensaje() {
    return idMensaje;
  }

  public String getNombres() {
    return nombres;
  }

  public void setNombres(String nombres) {
    this.nombres = nombres;
  }

  public String getCorreo() {
    return correo;
  }

  public void setCorreo(String correo) {
    this.correo = correo;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String tipo) {
    this.tipo = tipo;
  }

  public String getMensaje() {
    return mensaje;
  }

  public void setMensaje(String mensaje) {
    this.mensaje = mensaje;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public LocalDateTime getFechaEnvio() {
    return fechaEnvio;
  }
}
