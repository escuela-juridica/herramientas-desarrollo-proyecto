package pe.edu.escuela.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "administrador")
public class Administrador {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_admin")
  private Integer idAdmin;

  @Column(name = "nombres", nullable = false, length = 100)
  private String nombres;

  @Column(name = "apellidos", nullable = false, length = 100)
  private String apellidos;

  @Column(name = "correo", nullable = false, unique = true, length = 150)
  private String correo;

  @Column(name = "telefono", length = 20)
  private String telefono;

  @Column(name = "clave", nullable = false, length = 255)
  private String clave;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "estado", nullable = false, length = 1)
  private String estado = "A";

  @Column(name = "fecha_creacion", nullable = false, updatable = false)
  private LocalDateTime fechaCreacion;

  @Column(name = "fecha_modificacion")
  private LocalDateTime fechaModificacion;

  @Column(name = "fecha_ultimo_login")
  private LocalDateTime fechaUltimoLogin;

  @PrePersist
  protected void alCrear() {
    this.fechaCreacion = LocalDateTime.now();
  }

  @PreUpdate
  protected void alActualizar() {
    this.fechaModificacion = LocalDateTime.now();
  }

  public Integer getIdAdmin() {
    return idAdmin;
  }

  public void setIdAdmin(Integer idAdmin) {
    this.idAdmin = idAdmin;
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

  public String getClave() {
    return clave;
  }

  public void setClave(String clave) {
    this.clave = clave;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public LocalDateTime getFechaCreacion() {
    return fechaCreacion;
  }

  public LocalDateTime getFechaModificacion() {
    return fechaModificacion;
  }

  public LocalDateTime getFechaUltimoLogin() {
    return fechaUltimoLogin;
  }

  public void setFechaUltimoLogin(LocalDateTime fechaUltimoLogin) {
    this.fechaUltimoLogin = fechaUltimoLogin;
  }
}
