package pe.edu.escuela.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Persona que compra cursos. Se identifica por su DNI: si vuelve a comprar se reutiliza el mismo registro. */
@Entity
@Table(name = "cliente")
public class Cliente {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_cliente")
  private Integer idCliente;

  @Column(name = "nombres", nullable = false, length = 100)
  private String nombres;

  @Column(name = "apellidos", nullable = false, length = 100)
  private String apellidos;

  @Column(name = "documento", nullable = false, unique = true, length = 8)
  private String documento;

  @Column(name = "correo", nullable = false, length = 150)
  private String correo;

  @Column(name = "telefono", length = 20)
  private String telefono;

  @Column(name = "fecha_creacion", nullable = false, updatable = false)
  private LocalDateTime fechaCreacion;

  @PrePersist
  protected void alCrear() {
    this.fechaCreacion = LocalDateTime.now();
  }

  public String getNombreCompleto() {
    return nombres + " " + apellidos;
  }

  public Integer getIdCliente() {
    return idCliente;
  }

  public void setIdCliente(Integer idCliente) {
    this.idCliente = idCliente;
  }

  public String getNombres() {
    return nombres;
  }

  public void setNombres(String nombres) {
    this.nombres = nombres;
  }

  public String getApellidos() {
    return apellidos;
  }

  public void setApellidos(String apellidos) {
    this.apellidos = apellidos;
  }

  public String getDocumento() {
    return documento;
  }

  public void setDocumento(String documento) {
    this.documento = documento;
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

  public LocalDateTime getFechaCreacion() {
    return fechaCreacion;
  }
}
